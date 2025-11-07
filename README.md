# 게시판 기능 성능 측정 기록
## ERD

<img width="864" height="436" alt="ERD" src="https://github.com/user-attachments/assets/1433a9f5-ba23-4363-b983-6171af8bf987" />


## 성능 최적화 (N+1) 문제

### 1. 문제상황

게시글 목록 조회 API('GET /api/post-jpa')는 게시글 10개의 목록을 반환하는 요청입니다. 그러나 해당 API는 단순 Post(게시글) 엔티티 뿐만 아니라, User(사용자), Comment(댓글), PostLike(게시글 좋아요수) 정보를 함께 반환해야 했습니다.

하지만, JPA가 제공하는 기본 'findAll()' 메서드와 '.map()'을 이용한 초기 구현은 N+1 문제를 야기했습니다. 'p6spy'를 통해 확인한 결과, 10개의 게시글을 확인하기 위해 총 31번(1 + 10*3)의 쿼리가 실행되는 것을 확인했습니다.

### 2. N+1 현상

'p6spy'를 통해 확인한 결과, 게시글 목록 조회 API 요청 한번 당, 다음과 같이 한번의 메인 쿼리 수행 후 연관되어 있는 다른 엔티티를 반복해서 호출하는 것을 확인했습니다.


<img width="1364" height="436" alt="N+1 발생" src="https://github.com/user-attachments/assets/c7c9e4d6-54d5-4ccc-b12d-c94f867b3278" />


### 3. 문제 해결

게시글 목록 조회 API의 dto를 살펴보고 몇개의 엔티티클래스로부터 dto를 채워야하는지를 파악한 후, Service단에서 여러번의 엔티티 호출이 이루어지지 않도록 미리 객체를 JOIN FETCH하여 불필요한 쿼리요청을 줄이려고 했습니다. 그러나 Post-Comment의 관계는 1:N 이기에 하나의 게시글에 5개의 댓글이 달려있다면, 10개의 게시글 내에 동일한 게시글이 불필요하게 중복되어 포함될 수 있었기에 @Formula를 통해 해결할 수 있도록 하였습니다.

<img width="1275" height="222" alt="쿼리 속도 개선" src="https://github.com/user-attachments/assets/feb7fdbb-6cb2-4d72-ac2f-5545fb938294" />


