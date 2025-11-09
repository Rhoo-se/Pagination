package com.project.bulletin_board.repository;

import com.project.bulletin_board.entity.Comment;
import com.project.bulletin_board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPost(Post post);
}
