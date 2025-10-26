package com.project.bulletin_board.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignUpRequest {
    private String email;
    private String password;
    private String nickname;
}
