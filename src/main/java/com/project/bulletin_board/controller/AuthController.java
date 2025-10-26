package com.project.bulletin_board.controller;

import com.project.bulletin_board.dto.user.LoginRequest;
import com.project.bulletin_board.dto.user.LoginResponse;
import com.project.bulletin_board.dto.user.UserSignUpRequest;
import com.project.bulletin_board.jwt.JwtTokenProvider;
import com.project.bulletin_board.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = "신규 사용자의 회원가입 처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "존재하는 이메일 또는 닉네임")
    })
    @PostMapping("/signup")
    public ResponseEntity<String> signup (@RequestBody UserSignUpRequest userSignUpRequest){
        authService.signup(userSignUpRequest);
        return new ResponseEntity<>("회원가입 성공", HttpStatus.CREATED);
    }

    @Operation(summary = "로그인", description = "로그인 성공시 토큰을 반환함" )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "가입하지 않은 이메일이거나 잘못된 비밀번호")
    })
    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        String accessToken = authService.login(loginRequest);

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .build();
        return ResponseEntity.ok(loginResponse);
    }
}
