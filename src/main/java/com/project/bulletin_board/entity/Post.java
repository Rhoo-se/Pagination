package com.project.bulletin_board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private BoardInfo boardInfo;

    @Column(name = "title", nullable = false, unique = true, length = 30)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "like_count")
    private long likeCount = 0L;

    @Column(name = "comment_count")
    private long commentCount=0L;

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }
    public void decreaseLikeCount() {
        this.likeCount -= 1;
    }
    public void increaseCommentCount() {
        this.commentCount += 1;
    }
    public void decreaseCommentCount() {
        this.commentCount -= 1;
    }

}
