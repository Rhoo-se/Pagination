package com.project.bulletin_board.repository;

import com.project.bulletin_board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            "WHERE p.boardInfo.id = :boardId " +

            "AND (p.createdAt < :lastCreatedAt " +
            "    OR (p.createdAt = :lastCreatedAt AND p.id < :lastId)) " +
            "ORDER BY p.createdAt DESC, p.id DESC")
    List<Post> findNextPageByCreatedAt(
            @Param("boardId") Long boardId,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastId") Long lastId,
            Pageable pageable // 'LIMIT 10'을 위해 Pageable을 사용, 정수로 해도 되지만 이게 표준방법
    );

    @Modifying // CRUD 쿼리에 달아줌
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    // 좋아요 1 감소
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") Long postId);

    // 댓글 1 증가
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    // 댓글 1 감소
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId")
    void decrementCommentCount(@Param("postId") Long postId);


}
