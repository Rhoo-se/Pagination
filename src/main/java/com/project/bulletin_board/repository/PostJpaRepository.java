package com.project.bulletin_board.repository;

import com.project.bulletin_board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@EnableJpaRepositories(basePackages = "com.project.bulletin_board.repository")
@Repository
public interface PostJpaRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p " +
                    "FROM Post p " +
                    "WHERE p.boardInfo.id = :boardId",
            countQuery = "SELECT COUNT(p) " +
            "FROM Post p " +
            "WHERE p.boardInfo.id = :boardId")
    Page<Post> findAllWithUser(@Param("boardId") Long boardId, Pageable pageable);


    @Query("SELECT p FROM Post p " +
            "WHERE p.boardInfo.id = :boardId " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findFirstPageByCreatedAt(
            @Param("boardId") Long boardId, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.user u " + // ⭐️ N+1 방지용 JOIN FETCH
            "WHERE p.boardInfo.id = :boardId " +
            // ⭐️ 이 부분이 커서의 핵심: 마지막으로 본 글보다 '이전' 글을 찾음
            "AND (p.createdAt < :lastCreatedAt " +
            "    OR (p.createdAt = :lastCreatedAt AND p.id < :lastId)) " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findNextPageByCreatedAt(
            @Param("boardId") Long boardId,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt, // 👈 마지막 글의 생성일시
            @Param("lastId") Long lastId,                       // 👈 마지막 글의 ID
            Pageable pageable // 👈 'LIMIT 10'을 위해 Pageable을 사용
    );













}
