package com.project.bulletin_board.service;

import com.project.bulletin_board.dto.post.PageResponse;
import com.project.bulletin_board.dto.post.PostCreateRequest;
import com.project.bulletin_board.dto.post.PostListResponse;
import com.project.bulletin_board.dto.post.PostUpdateRequest;
import com.project.bulletin_board.entity.BoardInfo;
import com.project.bulletin_board.entity.Post;
import com.project.bulletin_board.entity.User;
import com.project.bulletin_board.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final BoardInfoJpaRepository boardInfoJpaRepository;
    private final PostJpaRepository postJpaRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;


    // 게시글 목록 조회 (offset방식)

    @Transactional(readOnly = true)
    public Page<PostListResponse> getPostList(Long boardId, Pageable pageable) {
        // 1. Repository 호출 (Post 엔티티 페이지 수신)
        //    (이때 N:1 JOIN FETCH와 1:N @Formula 서브쿼리가 모두 실행됨)
        Page<Post> postPage = postJpaRepository.findAllWithUser(boardId, pageable);

        // 2. DTO 페이지로 변환
        //    postPage.map(post -> new PostListResponse(post)) 와 동일
        Page<PostListResponse> dtoPage = postPage.map(PostListResponse::new);

        return dtoPage;
    }

    // 게시글 목록 조회 (cursor방식)

    @Transactional(readOnly = true)
    public List<PostListResponse> getPostListCursor(
            Long boardId,
            LocalDateTime lastCreatedAt,
            Long lastId,
            int size
    ) {

        // 1. 'SQL에서 LIMIT ?' 역할을 할 Pageable 객체를 수동으로 생성
        //    (JPA는 page 0, size 10 -> LIMIT 10으로 변환)
        // 이때 정수를 사용해줘도 되지만 Pageable과 PageRequest를 사용하는 것이 표준.
        // 위의 메소드와 차이점? 반환타입이 Page<T>냐 List<T>냐 다름. 그 이유는
        // Pageable은 입력이고 Page<T>나 List<T>는 출력임. 반환타입을 보고
        // 스프링에서 Pageable의 내용물을 결정하는데, Page<T>인 경우 Offset과 Limit을 모두,
        // List<T>인경우 Limit만을 요구함
        Pageable pageable = PageRequest.of(0, 10);

        // 2. Repository에서 Post 엔티티 목록 조회
        // (위의 기능에서 Page<Post>에 값을 넣어주는 것과 같은 이유)
        List<Post> posts;
        if (lastId != null && lastCreatedAt != null) {
            // ⭐️ [수정] "다음 페이지" 쿼리를 호출
            posts = postJpaRepository.findNextPageByCreatedAt(
                    boardId, lastCreatedAt, lastId, pageable
            );
        } else {
            // 2. else 조건: "첫 페이지" 조건 (커서가 하나라도 null일 때)
            // ⭐️ [수정] "첫 페이지" 쿼리를 호출
            posts = postJpaRepository.findFirstPageByCreatedAt(boardId, pageable);
        }
        // 3. List<Post> -> List<PostListResponse> DTO로 변환
        return posts.stream()
                .map(PostListResponse::new)
                .toList();
    }





    @Transactional
    public void createWithJpa(PostCreateRequest postCreateRequest, String email){

        User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원이라 글을 작성할 수 없습니다."));

        BoardInfo boardInfoEntity = boardInfoJpaRepository.findById(postCreateRequest.getBoardId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시판입니다."));

        Post post = Post.builder()
                .title(postCreateRequest.getTitle())
                .content(postCreateRequest.getContent())
                .user(userEntity)
                .boardInfo(boardInfoEntity)
                .viewCount(0L)
                .build();
        postJpaRepository.save(post);
        System.out.println(post.getTitle() + post.getContent());
    }



    //게시글 하나 읽기


    //게시글 수정

    @Transactional
    public void updatePost(Long postId, PostUpdateRequest postUpdateRequest, String email){

        Post post = postJpaRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 글입니다."));

        User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        if(!post.getUser().getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        post.update(postUpdateRequest.getTitle(), postUpdateRequest.getContent());
    }

    // 게시글 삭제 기능
    public void deletePost(Long postId, String email){

        User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        Post post = postJpaRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("이미 존재하지 않는 글입니다."));

        if(!post.getUser().getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }

        postJpaRepository.delete(post);
    }

}
