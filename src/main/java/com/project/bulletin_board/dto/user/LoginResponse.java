package com.project.bulletin_board.dto.user;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
}
