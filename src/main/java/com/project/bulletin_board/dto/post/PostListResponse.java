package com.project.bulletin_board.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.bulletin_board.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class PostListResponse {
    private Long postId;
    private Long rank;
    private String nickname;
    private String title;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdAt;

    public PostListResponse(Post post) {
        this.postId = post.getId();
        this.nickname = post.getUser().getNickname();
        this.title = post.getTitle();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.createdAt = post.getCreatedAt();
        this.rank = null;
    }
}
