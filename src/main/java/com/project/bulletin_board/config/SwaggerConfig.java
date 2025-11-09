package com.project.bulletin_board.config; // ⭐️ 회원님 패키지 경로에 맞게 수정

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 1. Swagger Info 설정 (선택 사항이지만 권장)
@OpenAPIDefinition(
        info = @Info(title = "게시판 서비스 API 명세서",
                description = "포트폴리오용 백엔드 API 명세서입니다.",
                version = "v1.0.0")
)
@Configuration
public class SwaggerConfig {

    // 2. ⭐️⭐️⭐️ JWT 인증 설정을 위한 Bean 등록 ⭐️⭐️⭐️
    @Bean
    public OpenAPI openAPI() {

        // 3. SecurityScheme의 이름 (arbitrary name)
        String jwtSchemeName = "bearerAuth";

        // 4. API 요청 헤더에 인증 정보를 담을 방식을 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP) // HTTP 방식
                .scheme("bearer")               // "bearer" 타입을 사용
                .bearerFormat("JWT");           // 토큰 형식은 JWT

        // 5. Swagger UI에 "Authorize" 버튼을 추가하고,
        //    모든 API에 전역적으로 위에서 정의한 SecurityScheme을 적용
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components().addSecuritySchemes(jwtSchemeName, securityScheme));
    }
}