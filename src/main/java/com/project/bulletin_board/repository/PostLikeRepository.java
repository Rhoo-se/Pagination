package com.project.bulletin_board.repository;

import com.project.bulletin_board.entity.Post;
import com.project.bulletin_board.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    long countByPost(Post post);
}
