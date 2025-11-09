package com.project.bulletin_board.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component // 이 필터를 Bean으로 등록
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 1. OncePerRequestFilter 상속

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService; // 2. 1단계에서 만든 "번역기" 주입

    // 3. 실제 필터링 로직은 doFilterInternal에 작성
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Request Header에서 "Authorization" 헤더를 찾음
        String token = resolveToken(request);

        // 2. 토큰이 유효한지 검사 (validateToken)
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰이 유효하면, 토큰에서 Subject(email)를 가져옴
            String email = jwtTokenProvider.getSubject(token);

            // 4. email로 UserDetailsService에서 UserDetails 객체를 가져옴
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5. "인증 객체" (Authentication) 생성
            // (UserDetails, [비밀번호는 null], [권한 목록])
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 6. ⭐️ Spring Security의 "인증된 사용자"로 등록 ⭐️
            // SecurityContextHolder에 이 인증 객체를 저장하면,
            // Spring Security가 이 요청을 "인증된" 것으로 간주합니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 7. 다음 필터 체인으로 요청을 전달
        filterChain.doFilter(request, response);
    }

    // "Authorization" 헤더에서 "Bearer " 부분을 떼어내고 토큰만 추출하는 헬퍼 메소드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).replaceAll("\\s", "");        }
        return null;
    }
}