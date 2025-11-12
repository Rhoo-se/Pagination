package com.project.bulletin_board.service;

import com.project.bulletin_board.entity.Post;
import com.project.bulletin_board.entity.PostLike;
import com.project.bulletin_board.entity.User;
import com.project.bulletin_board.repository.PostJpaRepository;
import com.project.bulletin_board.repository.PostLikeRepository;
import com.project.bulletin_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostJpaRepository postJpaRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    // 좋아요 누르기
    @Transactional
    public void addLike(Long postID, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
        Post post = postJpaRepository.findById(postID)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 글입니다."));

        if (postLikeRepository.existsByUserAndPost(user, post)) {
            throw new RuntimeException("이미 좋아요를 눌렀습니다.");
        }

        PostLike postlike = PostLike.builder()
                .user(user)
                .post(post)
                .build();
        postLikeRepository.save(postlike);

        postJpaRepository.incrementLikeCount(postID);
    }

    @Transactional
    public void removeLike(Long postId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Post post = postJpaRepository.findById(postId).orElseThrow();

        // 1. 좋아요 누른 기록 찾기
        PostLike postLike = postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new RuntimeException("좋아요 기록이 없습니다."));

        // 2. post_likes 테이블에서 기록 삭제
        postLikeRepository.delete(postLike);

        // 3.  posts 테이블의 like_count 1 감소 (Atomic 연산)
        postJpaRepository.decrementLikeCount(postId);
    }
}
