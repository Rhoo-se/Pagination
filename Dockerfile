# --- 1단계: 빌더(Builder) 스테이지 ---
# Gradle과 JDK 21을 포함한 이미지에서 빌드를 수행합니다.
FROM gradle:8.8.0-jdk21 AS builder

# 소스 코드를 복사합니다.
WORKDIR /app
COPY . .

# Gradle을 사용하여 프로젝트를 빌드합니다. (테스트는 생략하여 속도 향상)
RUN ./gradlew build --no-daemon -x test
# (만약 gradlew 실행 권한이 없다면: RUN chmod +x ./gradlew && ./gradlew build --no-daemon -x test)


# --- 2단계: 최종(Final) 스테이지 ---
# 실제 실행에 필요한 최소한의 이미지(JRE)를 사용합니다.
FROM amazoncorretto:21-alpine

WORKDIR /app

# 1단계(builder)에서 빌드된 .jar 파일만 복사해옵니다.
# 경로를 정확하게 확인하세요 (대개 build/libs/...)
COPY --from=builder /app/build/libs/bulletin-board-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]