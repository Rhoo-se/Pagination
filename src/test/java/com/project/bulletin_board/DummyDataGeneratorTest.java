package com.project.bulletin_board;

import com.project.bulletin_board.entity.BoardInfo;
import com.project.bulletin_board.entity.Post;
import com.project.bulletin_board.entity.User;
import com.project.bulletin_board.repository.BoardInfoJpaRepository;
import com.project.bulletin_board.repository.PostJpaRepository;
import com.project.bulletin_board.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class DummyDataGeneratorTest {

    @Autowired private PostJpaRepository postJpaRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BoardInfoJpaRepository boardInfoJpaRepository;
    @Autowired private EntityManager entityManager;

    // ⭐️ 2. TransactionTemplate 주입
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
        // ⭐️ 3. 메서드의 @Transactional과 @Commit 제거!
        // @Transactional
        // @Commit
    void generateDummyPosts() {
        // User와 BoardInfo는 트랜잭션 밖에서 미리 조회해도 괜찮습니다.
        // (단, 이 조회를 위해선 @Transactional이 필요할 수 있으니,
        //  별도 메서드 @Transactional(readOnly=true)로 빼거나,
        //  아래 execute 안으로 넣는 것이 더 안전합니다.)

        // ⭐️ 3-1. (더 안전한 방식) 조회 로직도 트랜잭션 안에서 처리
        // User testUser = userRepository.findByEmail("test@test.com").orElseThrow();
        // BoardInfo freeBoard = boardInfoJpaRepository.findById(1L).orElseThrow();

        int batchSize = 1000;
        int totalInserts = 1000000;

        System.out.println("--- 100만 개 데이터 생성 시작 (작은 트랜잭션 1000번) ---");

        for (int i = 0; i < totalInserts / batchSize; i++) {

            // ⭐️ 1. 'i'의 현재 값을 'final' 변수에 복사
            //    (이 변수는 루프가 돌 때마다 새로 생성되며, 변경되지 않음)
            final int currentIteration = i;

            transactionTemplate.execute(status -> {
                User testUser = userRepository.findByEmail("test@test.com").orElseThrow();
                BoardInfo freeBoard = boardInfoJpaRepository.findById(1L).orElseThrow();

                List<Post> batch = new ArrayList<>();
                for (int j = 0; j < batchSize; j++) {

                    // ⭐️ 2. 변경되는 'i' 대신, 'final' 변수인 'currentIteration'을 사용
                    int postNumber = (currentIteration * batchSize) + j + 1;

                    Post post = Post.builder()
                            .title("JPA 테스트 글 " + postNumber)
                            .content("성능 테스트용 더미 데이터입니다. " + postNumber)
                            .user(testUser)
                            .boardInfo(freeBoard)
                            .viewCount(0L)
                            .build();
                    batch.add(post);
                }

                postJpaRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();

                return null;
            });

            if ((i + 1) % 100 == 0) {
                System.out.println("--- " + (i + 1) * batchSize + "개 생성 완료 ---");
            }
        }
        System.out.println("--- 100만 개 데이터 생성 완료 ---");
    }
}