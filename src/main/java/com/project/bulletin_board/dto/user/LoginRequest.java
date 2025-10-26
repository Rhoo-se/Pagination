package com.project.bulletin_board.dto.user;


import lombok.Getter;

@Getter
public class LoginRequest {
    private String email;
    private String password;
}
