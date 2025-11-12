package com.project.bulletin_board.repository;

import com.project.bulletin_board.entity.Post;
import com.project.bulletin_board.entity.PostLike;
import com.project.bulletin_board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    long countByPost(Post post);
    boolean existsByUserAndPost(User user, Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
}
