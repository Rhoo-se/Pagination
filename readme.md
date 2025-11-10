
# 게시판 기능 성능 측정 기록

## ERD

<img width="864" height="436" alt="ERD" src="[https://github.com/user-attachments/assets/1433a9f5-ba23-4363-b983-6171af8bf987](https://github.com/user-attachments/assets/1433a9f5-ba23-4363-b983-6171af8bf987)" />

데이터 **정규화**를 위해 `posts`와 `post_likes` 테이블을 분리했고, `comments`와 `comment_likes`도 분리하였습니다.

-----

## 1. 성능 최적화 (N+1) 문제

### 1-1. 문제상황

게시글 목록 조회 API('GET /api/post-jpa')는 게시글 10개의 목록을 반환하는 요청입니다. 

이에 해당 API는 단순 Post(게시글) 엔티티 뿐만 아니라 작성자의 **'닉네임'**, 해당 게시글에 달린 **'댓글의 개수'** 및 **'좋아요 개수'**를 함께 반환해야 했습니다.

하지만 JPA가 제공하는 기본 'findAll()' 메서드와 '.map()'을 이용한 초기 구현은 **N+1 문제**를 야기했습니다.

'p6spy'를 통해 확인한 결과, 10개의 게시글을 확인하기 위해 총 **31번(1  10*3)**의 쿼리가 실행되는 것을 확인했습니다.

### 1-2. N+1 현상 (Before)

'p6spy'를 통해 확인한 결과, 게시글 목록 조회 API 요청 한번 당, 다음과 같이 한번의 메인 쿼리 수행 후 연관되어 있는 다른 엔티티를 반복해서 호출하는 것을 확인했습니다.

<details>
<summary>
<b>[증거] N+1 발생 p6spy 로그 (클릭하여 펼치기)</b>
</summary>

<img width="1364" height="436" alt="N+1 발생" src="[https://github.com/user-attachments/assets/c7c9e4d6-54d5-4ccc-b12d-c94f867b3278](https://github.com/user-attachments/assets/c7c9e4d6-54d5-4ccc-b12d-c94f867b3278)" />
</details>

### 1-3. 문제 해결 (After)

게시글 목록 조회 API의 dto를 살펴보고 몇개의 엔티티클래스로부터 dto를 채워야하는지를 파악한 후, Service단에서 여러번의 엔티티 호출이 이루어지지 않도록 미리 객체를 `JOIN FETCH`하여 불필요한 쿼리요청을 줄이려고 했습니다.

그러나 Post-Comment와 Post-PostLike의 관계는 **1:N** 이기에 하나의 게시글에 5개의 댓글이 달려있다면, 10개의 게시글 내에 동일한 게시글이 불필요하게 중복되어 포함될 수 있었기에 **`@Formula`**를 통해 해결할 수 있도록 하였습니다.

<details>
<summary>
<b>[증거] N+1 문제 해결 후 쿼리 로그 (클릭하여 펼치기)</b>
</summary>

<img width="1275" height="222" alt="쿼리 속도 개선" src="[https://github.com/user-attachments/assets/feb7fdbb-6cb2-4d72-ac2f-5545fb938294](https://github.com/user-attachments/assets/feb7fdbb-6cb2-4d72-ac2f-5545fb938294)" />
</details>

-----

## 2. Offset 기반 페이지네이션 및 커서 기반 페이지네이션 적용

### 2-1. 문제상황

게시글 목록 조회 API는 `Pageable`을 사용한 기본 **Offset 방식**으로 작동하고 최신 글목록 10개를 반환합니다. 그렇기 때문에 앞쪽 페이지를 조회할 때는 빠르지만, **`Offset`이 500,000 `limit` 10**과 같은 명령을 받게되면 MySQL은 **500,010개의 행을 전부 스캔**해야하기에 비효율적으로 작동합니다.

따라서 인스타그램이나 페이스북처럼 스크롤을 통해 연속적으로 피드를 확인하는 게시판을 만들 경우 오프셋 방식으로 구현할 시 반복적으로 수많은 행을 스캔하여 비효율적이라는 사실을 알게되었습니다. 실제로 더미데이터 **100만건**을 생성한 후, 50,001번째 페이지부터 10개의 게시글 목록을 요청하였을 경우 다음과 같이 시간이 걸리는 것을 확인했습니다. 

### 2-2. 깊은 페이지 조회 문제

'p6spy'를 통해 확인한 결과, 50,001번째 페이지를 찾는 것과 50,002번째 페이지를 찾는 것 모두 약 **4000~4300ms**가 걸린 것을 확인할 수 있었습니다.

최신순으로 글의 목록을 정렬할 때, 인덱스가 아닌 기본 **`filesort`**를 통해 정렬하였기에 약 4초라는 긴 시간이 소요되었음을 확인하였습니다.

<details>
<summary>
<b>[증거] Offset 깊은 페이지 조회 p6spy 로그 (클릭하여 펼치기)</b>
</summary>

[측정결과]
<img width="1277" height="226" alt="Offset 쿼리속도1" src="[https://github.com/user-attachments/assets/33796dc2-edb9-487e-a101-ee9ea5afa80c](https://github.com/user-attachments/assets/33796dc2-edb9-487e-a101-ee9ea5afa80c)" />

<img width="1207" height="160" alt="Offset 쿼리속도2" src="[https://github.com/user-attachments/assets/3f34d045-500a-4def-92ea-0d24a6d28d21](https://github.com/user-attachments/assets/3f34d045-500a-4def-92ea-0d24a6d28d21)" />

<img width="818" height="169" alt="Offset 예상 스캔 횟수" src="[https://github.com/user-attachments/assets/eaaf8e9d-d860-49b8-9078-71b5e8c3e24b](https://github.com/user-attachments/assets/eaaf8e9d-d860-49b8-9078-71b5e8c3e24b)" />
</details>

-----

### 3-1. 1차 개선 시도: JOIN FETCH와 filesort의 충돌

**[문제]**  
API의 속도 향상을 위해 `posts`테이블의 `created_at`과 `id`를 Key값으로 두어 **B-Tree 인덱스**를 설정하였습니다.

하지만 100만건에 대해 테스트한 결과, 1페이지를 조회시에도 `ORDER BY`(정렬)가 인덱스를 이용하지 않고, 100만건 전체를 **`filesort`**하는 현상이 발견되었습니다.

**[원인 분석]**  
처음 Offset방식의 쿼리 N+1 문제를 해결 할 때, **`LEFT JOIN FETCH p.user`**를 사용했습니다. (Post엔티티와 User엔티티의 JOIN FETCH)

하지만 `LEFT JOIN FETCH`와 `Pageable`의 `ORDER BY`가 충돌하여 인덱스기준 정렬이 아닌 `filesort`가 실행되었고, 이로인해 성능의 향상이 이루어지지 않았습니다.

`ORDER BY`를 효율적으로 지원하는 인덱스가 정해지지 않은 상황에서 `ORDER BY`와 같은 데이터의 정렬이 일어나게되면, 실시간으로 `filesort`를 실행하였기에 오래걸렸던 것이었습니다.

**[개선이 필요한 사항]**  
인덱스 방식의 정렬을 위해 `JOIN FETCH`의 방식을 포기하고, 이로 인해 다시 생길 N+1 문제를 해결하기 위해 **`@BatchSize`**를 활용해보기로 했습니다.

### 3-2. 2차 개선 시도: JOIN FETCH 제거, @BatchSize를 통한 N+1 문제 해결

**[문제]**  
`filesort` 문제를 해결하기 위해, `LEFT JOIN FETCH`를 제거하였지만, 다시 **N+1 문제**가 발생하게 되었습니다.

**[해결]**  
이에 `User` 엔티티 클래스에 **`@BatchSize(size = 100)`**를 적용하였습니다.

<details>
<summary>
<b>[증거] @BatchSize 적용 후 p6spy 로그 (클릭하여 펼치기)</b>
</summary>

[1페이지 조회시 걸리는 시간]
<img width="1194" height="266" alt="@BatchSize 적용 후 1페이지 조회" src="[https://github.com/user-attachments/assets/214c578f-d129-4f4c-bcc0-68ad8db27090](https://github.com/user-attachments/assets/214c578f-d129-4f4c-bcc0-68ad8db27090)" />

[50,001페이지 조회시 걸리는 시간]
<img width="1194" height="264" alt="@BatchSize적용후 50,001페이지 조회" src="[https://github.com/user-attachments/assets/1acdb52a-ced1-4ad2-91b5-2428907de641](https://github.com/user-attachments/assets/1acdb52a-ced1-4ad2-91b5-2428907de641)" />

</details>

기존 약 **4000ms**가 걸렸던 API 요청이 인덱스를 적용한 결과 얕은 페이지는 약 **400ms**, 깊은 페이지는 약 **800ms**만에 실행되었습니다.

**[개선이 필요한 사항]**  
`filesort` 문제는 해결되었지만 `Offset` 방식의 근본적인 한계로 인해 여전히 1페이지에 비해 2배가량 느림.

### 3-3. 3차 개선 시도 : No-offset(커서 기반) 적용

**[문제]**  
인덱스를 통해 성능의 향상이 이루어졌지만 Offset방식의 특성상 깊은 페이지에 대한 요청은 여전히 1페이지에 비해 2배가량 느린 것을 확인했습니다.

**[해결]**  
`lastId`, `lastCreatedAt`의 값을 통해 사용자가 가장 최근에 조회한 게시글 목록을 확인하고 그곳에서부터 스캔을 하는 **커서 기반 방식**을 적용하였습니다.

또한 커서 기반 페이지네이션은 `Page`객체를 사용하지 않기 때문에 **총 게시물의 개수를 세지 않아도 되므로**(`countQuery` 생략) 1페이지 요청에 대해 약 240ms 속도 향상도 일어나게되었습니다.

<details>
<summary>
<b>[증거] 커서 방식 p6spy 로그 (클릭하여 펼치기)</b>
</summary>

[1페이지 조회시 걸리는 시간 : 약 160ms]  
<img width="1208" height="289" alt="커서 방식 1페이지 조회" src="[https://github.com/user-attachments/assets/70f758c4-f190-48a8-b546-d66bc3e34133](https://github.com/user-attachments/assets/70f758c4-f190-48a8-b546-d66bc3e34133)" />
(약 160ms로 Offset 방식에 비해 약40ms 빨라짐)

[50,001페이지 조회시 걸리는 시간 : 약 2000ms]   
<img width="1206" height="166" alt="커서 방식 50000페이지 조회" src="[https://github.com/user-attachments/assets/c7e80432-a09b-4d1c-bcca-9ee97c76806a](https://github.com/user-attachments/assets/c7e80432-a09b-4d1c-bcca-9ee97c76806a)" />
(3-2의 상황보다 약1200ms 느려짐)

</details>

**[개선이 필요한 사항 (새로운 문제 발견)]**  
기존 Offset방식에서 Cursor 방식으로 바꾸었지만, 다시 깊은 페이지에 대한 요청이 1페이지에 대한 요청에 비해 현저하게 느려지는 **새로운 문제**를 발견하였습니다. 

**(이곳에 `EXPLAIN` 캡처를 첨부하여 `filesort` 증거 제시)**

원인을 파악해본 결과 N+1 문제를 해결하기 위해 사용했던 **`@Formula`**의 쿼리가 복잡하여 MySQL 옵티마이저가 Cursor방식에서 (`created_at`, `id`) 인덱스를 이용하지 않고 **`filesort`**방식을 채택한 것이 원인이었습니다.

### 3-4. 4차 개선 시도 : 비정규화를 통한 성능 향상

**[문제]**  
`@Formula` 방식은 정확하고 편리하게 원하는 테이블을 만들 수 있었지만, MySQL 옵티마이저의 한계에 부딪혀 여전히 깊은 페이지에 대한 요청에서는 `filesort`를 유발했습니다.

**[해결]**  
`posts`와 1:N 관계에 있는 `post_likes`와 `comments`를 스칼라 서브쿼리로 읽어들이는 것이 아닌, `posts`테이블에 속성으로 추가하도록 **테이블을 수정(비정규화)**하였습니다.

또한 데이터 정규화를 위해 분리했던 기존의 DB가 바뀌게 됨에 따라 `like_count` 업데이트시 발생할 수 있는 **새로운 동시성(경쟁 상태) 문제**를 어떻게 해결할 것인지 다시 한번 고민해 보았습니다.

이에 트랜잭션의 기본 원칙인 **원자성**을 이용할 수 있도록 **`@Modifying @Query`**를 사용하여 해결하였습니다. 

즉, 서버에서 DB의 데이터를 읽고 수정하는 여러번의 쿼리가 아닌, **하나의 쿼리문**에서 현재 값을 읽고 곧바로 상태를 수정할 수 있도록 하여 경쟁 상태를 해소하였습니다.

<details>
<summary>
<b>[최종 증거] 비정규화  커서 방식 p6spy 로그 (클릭하여 펼치기)</b>
</summary>

[1페이지 조회시 걸리는 시간 : 약 5ms]  
<img width="1270" height="244" alt="커서 방식 1페이지 조회(비정규화 방식)" src="[https://github.com/user-attachments/assets/1fc52a20-e61b-42ce-99d6-98061c57e48c](https://github.com/user-attachments/assets/1fc52a20-e61b-42ce-99d6-98061c57e48c)" />

[50,001페이지 조회시 걸리는 시간 : 약 3ms]  
<img width="1285" height="228" alt="커서 방식 50001페이지 조회(비정규화방식)" src="[https://github.com/user-attachments/assets/744fb232-ce59-49bc-9756-c2ebdf10969f](https://github.com/user-attachments/assets/744fb232-ce59-49bc-9756-c2ebdf10969f)" />

</details>

1페이지 조회시, 50,001페이지 두 방식 모두다 **5ms 이하**로 확연하게 빠르게 작동하였습니다.

#### 📊 최종 성능 비교 요약 (칸막이)

| 구분 (100만 건 데이터 기준) | 1 페이지 (`page=0`) | 50,001 페이지 (`page=50000`) |
| :--- | :---: | :---: |
| **개선 전 (Offset  `JOIN FETCH`)** | `~ 3,000ms` (filesort) | `~ 4,000ms` (filesort  offset) |
| **개선 후 (Cursor  비정규화)** | `~ 5ms` | `~ 3ms` |

-----

### 3-5. 문제 해결 (최종 결론)

깊은페이지에 대한 성능 문제를 해결하면서, N+1 문제에 대한 다양한 해결방법을 공부하게 되었습니다. ( **`JOIN FETCH`**, **`@Formula`**, **`@BatchSize`** )

또한 DB에서 데이터를 정렬하여 읽어 들일 때 기준이 없다면 `filesort`가 일어나 데이터 스캔시 많은 시간이 소요되지만,

기존 느린 `ORDER BY` 쿼리에 맞추어 **B-Tree로 복합 인덱스를 생성**한다면 `filesort`를 제거하고 인덱스 스캔을 최소화할 수 있다는 것을 알 수 있게 되었습니다.

마지막으로 **Offset방식과 Cursor방식의 비교**를 통해, 사용자의 요구에 따라 페이지 점프 기능이 필요하다면 Offset방식으로, 빠른 데이터 로딩이 필요하다면 Cursor방식으로 상황에 맞춰 적절하게 구현해야 한다는 것을 깨달았습니다.
