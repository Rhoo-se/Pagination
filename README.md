# 게시판 기능 성능 측정 기록
## ERD

<img width="864" height="436" alt="ERD" src="https://github.com/user-attachments/assets/1433a9f5-ba23-4363-b983-6171af8bf987" />


## 성능 최적화 (N+1) 문제

### 1. 문제상황

게시글 목록 조회 API('GET /api/post-jpa')는 게시글 10개의 목록을 반환하는 요청입니다. 그러나 해당 API는 단순 Post(게시글) 엔티티 뿐만 아니라, User(사용자), Comment(댓글), PostLike(게시글 좋아요수) 정보를 함께 반환해야 했습니다.

하지만, JPA가 제공하는 기본 'findAll()' 메서드와 '.map()'을 이용한 초기 구현은 N+1 문제를 야기했습니다. 'p6spy'를 통해 확인한 결과, 10개의 게시글을 확인하기 위해 총 31번(1 + 10*3)의 쿼리가 실행되는 것을 확인했습니다.

### 2. N+1 현상 (Before)

'p6spy'를 통해 확인한 결과, 게시글 목록 조회 API 요청 한번 당, 다음과 같이 한번의 메인 쿼리 수행 후 연관되어 있는 다른 엔티티를 반복해서 호출하는 것을 확인했습니다.


<img width="1364" height="436" alt="N+1 발생" src="https://github.com/user-attachments/assets/c7c9e4d6-54d5-4ccc-b12d-c94f867b3278" />


### 3. 문제 해결 (After)

게시글 목록 조회 API의 dto를 살펴보고 몇개의 엔티티클래스로부터 dto를 채워야하는지를 파악한 후, Service단에서 여러번의 엔티티 호출이 이루어지지 않도록 미리 객체를 JOIN FETCH하여 불필요한 쿼리요청을 줄이려고 했습니다. 그러나 Post-Comment의 관계는 1:N 이기에 하나의 게시글에 5개의 댓글이 달려있다면, 10개의 게시글 내에 동일한 게시글이 불필요하게 중복되어 포함될 수 있었기에 @Formula를 통해 해결할 수 있도록 하였습니다.

<img width="1275" height="222" alt="쿼리 속도 개선" src="https://github.com/user-attachments/assets/feb7fdbb-6cb2-4d72-ac2f-5545fb938294" />


## Offset 기반 페이지네이션 및 커서 기반 페이지네이션 적용

### 1. 문제상황

게시글 목록 조회 API는 Pageable을 사용한 기본 Offset 방식으로 작동하고 최신 글목록 10개를 반환합니다. 그렇기 때문에 앞쪽 페이지를 조회할 때는 빠르지만, Offset이 500,000 limit 10과 같은 명령을 받게되면 MySQL은 500,010개의 행을 전부 스캔해야하기에 비효율적으로 작동합니다. 따라서 인스타그램이나 페이스북처럼 스크롤을 통해 연속적으로 피드를 확인하는 게시판을 만들 경우 오프셋 방식으로 구현할 시 반복적으로 수많은 행을 스캔하여 비효율적이라는 사실을 알게되었습니다. 실제로 더미데이터 100만건을 생성한 후, 50만번째의 게시글부터 10개의 게시글 목록을 요청하였을 경우 다음과 같이 시간이 걸리는 것을 확인했습니다. 


### 2. 깊은 페이지 조회 문제

'p6spy'를 통해 확인한 결과, 50,001번째 페이지를 찾는 것과 50,002번째 페이지를 찾는 것 모두 약 4000~4300ms가 걸린 것을 확인할 수 있었습니다.

최신순으로 글의 목록을 정렬할 때, 인덱스가 아닌 기본 filesort를 통해 정렬하였기에 약 4초라는 긴 시간이 소요되었음을 확인하였습니다.

[측정결과]
<img width="1277" height="226" alt="Offset 쿼리속도1" src="https://github.com/user-attachments/assets/33796dc2-edb9-487e-a101-ee9ea5afa80c" />

<img width="1207" height="160" alt="Offset 쿼리속도2" src="https://github.com/user-attachments/assets/3f34d045-500a-4def-92ea-0d24a6d28d21" />

<img width="818" height="169" alt="Offset 예상 스캔 횟수" src="https://github.com/user-attachments/assets/eaaf8e9d-d860-49b8-9078-71b5e8c3e24b" />



### 3-1. 1차 개선 시도: JOIN FETCH와 filesort의 충돌

[문제]
API의 속도 향상을 위해 글의 작성일자와 글의 id를 Key값으로 두어 인덱스를 설정하였습니다.

하지만 100만건에 대해 테스트한 결과, 1페이지를 조회시에도 ORDER BY가 인덱스를 이용하지 않고, 100만건 전체를 filesort하는 현상이 발견되었습니다.

[원인 분석]
처음 Offset방식의 쿼리 N+1 문제를 해결 할 때, LEFT JOIN FETCH p.user를 사용했습니다.

하지만 LEFT JOIN FETCH와 Pageable의 ORDER BY가 충돌하여 인덱스가 아닌 filesort가 실행하였고, 이로인해 성능의 향상이 이루어지지 않았습니다.


### 3-2. 2차 개선 시도: @BatchSize를 통한 N+1 문제 해결

[문제] filesort 문제를 해결하기 위해, LEFT JOIN FETCH를 제거하였지만, 다시 N+1 문제가 발생하게 되었습니다.

[해결] 이에 엔티티 클래스에 @BatchSize(size = 100)를 적용하였습니다.

[1페이지 조회시 걸리는 시간]
<img width="1194" height="266" alt="@BatchSize 적용 후 1페이지 조회" src="https://github.com/user-attachments/assets/214c578f-d129-4f4c-bcc0-68ad8db27090" />


[50,001페이지 조회시 걸리는 시간]
<img width="1194" height="264" alt="@BatchSize적용후 50,001페이지 조회" src="https://github.com/user-attachments/assets/1acdb52a-ced1-4ad2-91b5-2428907de641" />

기존 약4000ms가 걸렸던 API 요청이 인덱스를 통해, 얕은 페이지는 약400ms, 깊은 페이지는 약800ms만에 실행되었습니다.



### 3-3. 3차 개선 시도 : No-offset(커서 기반) 적용

인덱스를 통해 성능의 향상이 이루어졌지만 Offset방식의 특성상 깊은 페이지에 대한 요청은 여전히 1페이지에 비해 2배가량 느린 것을 확인했습니다.

이에 lastId, lastCreatedAt의 값을 통해 사용자가 가장 최근에 조회한 게시글 목록을 확인하고 그곳에서부터 스캔을 하는 커서 기반 방식을 적용하였습니다.

또한 커서 기반 페이지네이션은 Page객체를 사용하지 않기 때문에 총 게시물의 개수를 세지 않아도 되므로 약간의 속도 향상도 일어나게되었습니다.



#### Cursor + @BatchSize를 적용하여 걸린 시간

[1페이지 조회시 걸리는 시간]

<img width="1208" height="289" alt="커서 방식 1페이지 조회" src="https://github.com/user-attachments/assets/70f758c4-f190-48a8-b546-d66bc3e34133" />

(약 160ms로 Offset 방식에 비해 약40ms 빨라짐)

[50,001페이지 조회시 걸리는 시간]

다만 기능 요구에 따라 페이지를 점프하는 기능이 포함되어야 한다면 Offset방식으로, 무한 스크롤방식으로 구현해야 한다면 Cursor 방식으로 해야한다는 것을 알게되었습니다.


### 3-4. 4차 개선 시도 : @Formula와 filesort의 충돌, 비정규화를 통한 

