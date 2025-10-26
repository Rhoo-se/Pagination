package com.project.bulletin_board.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
// 1. Swagger 문서의 기본 정보를 설정합니다. (제목, 설명, 버전)
@OpenAPIDefinition(
        info = @Info(title = "게시판 프로젝트 API 명세서",
                description = "포트폴리오용 게시판 서비스 API입니다.",
                version = "v1.0.0")
)
@Configuration
public class SwaggerConfig {

    // 2. Swagger UI에서 JWT 인증을 사용할 수 있도록 "Authorize" 버튼을 설정합니다.
    @Bean
    public OpenAPI openAPI() {

        // JWT 인증 스키마 이름을 "JWT Auth"로 정의
        String securitySchemeName = "JWT Auth";

        // SecurityScheme: 인증 방식을 정의합니다. (Bearer Token 사용)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // HTTP 방식
                .scheme("bearer") // "Bearer" 토큰 사용
                .bearerFormat("JWT") // 토큰 형식은 JWT
                .in(SecurityScheme.In.HEADER) // 토큰은 헤더에 담김
                .name("Authorization"); // 헤더 이름은 "Authorization"

        // SecurityRequirement: API 요청 시 "JWT Auth" 인증이 필요하다고 명시
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);

        return new OpenAPI()
                // Components에 위에서 정의한 SecurityScheme을 추가
                .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme))
                // 모든 API 요청에 SecurityRequirement를 적용
                .addSecurityItem(securityRequirement);
    }
}
*/