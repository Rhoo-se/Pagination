package com.project.bulletin_board.service;
import com.project.bulletin_board.entity.Comment;
import com.project.bulletin_board.entity.Post;
import com.project.bulletin_board.entity.User;
import com.project.bulletin_board.repository.CommentRepository;
import com.project.bulletin_board.repository.PostJpaRepository;
import com.project.bulletin_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostJpaRepository postJpaRepository; // ⭐️ @Modifying 쿼리용
    private final UserRepository userRepository;

    @Transactional
    public void createComment(Long postId, String content, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Post post = postJpaRepository.findById(postId).orElseThrow();

        // 1. comments 테이블에 댓글 저장
        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .build();
        commentRepository.save(comment);

        // 2. posts 테이블의 comment_count 1 증가 (Atomic 연산)
        postJpaRepository.incrementCommentCount(postId);
    }

    // [ 댓글 삭제 ]
    @Transactional
    public void deleteComment(Long commentId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        // 1. 댓글 찾기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 없습니다."));

        // 2. (보안) 본인 댓글인지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("삭제 권한이 없습니다."); // (AccessDeniedException 권장)
        }

        // 3. comments 테이블에서 댓글 삭제
        commentRepository.delete(comment);

        // 4.️ posts 테이블의 comment_count 1 감소 (Atomic 연산)
        //    (댓글 엔티티에서 postId를 꺼내서 사용)
        postJpaRepository.decrementCommentCount(comment.getPost().getId());
    }
}
