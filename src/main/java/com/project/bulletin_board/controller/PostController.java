package com.project.bulletin_board.controller;


import com.project.bulletin_board.dto.post.PageResponse;
import com.project.bulletin_board.dto.post.PostCreateRequest;
import com.project.bulletin_board.dto.post.PostListResponse;
import com.project.bulletin_board.dto.post.PostUpdateRequest;
import com.project.bulletin_board.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts-jpa")
public class PostController {
    private final PostService postService;

    //특정 게시판 게시글 전체 조회 (Offset 방식)
    @Operation(summary = "글 목록 조회 (페이징)", description = "특정 게시판의 게시글 목록을 오프셋 방식으로 10개씩 페이징하여 조회합니다.")
    @GetMapping("/offset")
    public ResponseEntity<PageResponse<PostListResponse>> getPostList(
            @RequestParam("boardId") Long boardId,
            @ParameterObject Pageable pageable){
        Page<PostListResponse> postList = postService.getPostList(boardId, pageable);
        return ResponseEntity.ok(new PageResponse<>(postList));
    }


    //특정 게시판 게시글 전체 조회 (Cursor 방식)
    @Operation(summary = "글 목록 조회 (커서)", description = "특정 게시판의 게시글 목록을 커서방식으로 10기씩 페이징 하여 조회합니다.")
    @GetMapping("/cursor")
    public ResponseEntity<List<PostListResponse>> getPostListCursor(
            @RequestParam Long boardId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime lastCreatedAt,

            @RequestParam(required = false)
            Long lastId,

            @RequestParam(defaultValue = "10")
            int size
    ){
        List<PostListResponse> postList = postService.getPostListCursor(
                boardId, lastCreatedAt, lastId, size
        );
        return ResponseEntity.ok(postList);
    }

    //특정 게시글 생성 기능 (C)
    @Operation(summary = "글쓰기 기능(jpa)", description = "게시판에 글을 쓰는 기능")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "글쓰기 성공"),
            @ApiResponse(responseCode = "400", description = "제목과 내용의 조건을 지키지 못했음"),
            @ApiResponse(responseCode = "401", description = "로그인 유저만 글을 쓸 수 있음")
    })
    @PostMapping
    public ResponseEntity<String> createPost (
            @RequestBody @Valid PostCreateRequest postCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        postService.createWithJpa(postCreateRequest, userEmail);

        return new ResponseEntity<>("게시글 작성 완료", HttpStatus.CREATED);
    }
    //특정 게시글 1개 조회 기능 (R)

    @GetMapping("/{postId}")
    public ResponseEntity<String> readPost (
            @PathVariable Long postId) {
        return null;
    }

    //특정 게시글 1개 수정 기능 (U)
    @Operation(summary = "게시글 수정", description = "특정 게시글 하나를 수정하는 기능")
    @PutMapping("/{postId}")
    public ResponseEntity<String> updatePost(
            @PathVariable Long postId,
            @RequestBody @Valid PostUpdateRequest postUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userMail = userDetails.getUsername();

        postService.updatePost(postId, postUpdateRequest, userMail);

        return ResponseEntity.ok("게시글 수정 완료");
    }
    //특정 게시글 1개 삭제 기능 (D)

    @Operation(summary = "게시글 삭제 기능", description = "특정 게시글 하나를 삭제하는 기능")
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost (
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userMail = userDetails.getUsername();
        postService.deletePost(postId, userMail);
        return ResponseEntity.ok("삭제가 완료되었습니다.");
    }
}
