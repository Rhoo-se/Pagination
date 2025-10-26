package com.project.bulletin_board.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 4. CSRF 보호 비활성화 (API 서버에서는 불필요)
                .csrf(csrf -> csrf.disable())
                // 5. 기본 HTTP 인증(Basic Auth) 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())
                // 6. 폼 로그인 비활성화
                .formLogin(formLogin -> formLogin.disable())
                // 7. 세션 관리 상태 없음(Stateless)으로 설정 (JWT 사용 준비)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 8. ⭐️ 핵심: API 경로별 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // "/api/auth/**" (회원가입, 로그인) 경로는 모두 허용
                        .requestMatchers("/**").permitAll()
                );
        return http.build();
    }
}
