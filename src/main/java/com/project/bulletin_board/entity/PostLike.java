package com.project.bulletin_board.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_post", // (이 이름은 아무거나 쓰셔도 됩니다)
                        columnNames = {"user_id", "post_id"}
                )
        }
)
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

//    create table post_likes(
//            id BIGINT PRIMARY KEY AUTO_INCREMENT,
//            user_id BIGINT NOT NULL REFERENCES users(id),
//    post_id BIGINT NOT NULL REFERENCES posts(id),
//    UNIQUE key uk_user_post (user_id, post_id)
//);
}
