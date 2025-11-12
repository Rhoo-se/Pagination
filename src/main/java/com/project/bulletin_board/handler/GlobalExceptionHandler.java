package com.project.bulletin_board.handler;


import com.project.bulletin_board.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/*
200 OK: 요청 성공 (조회 성공)
201 Created: 요청 성공 + 새 리소스 생성됨 (회원가입/글쓰기 성공)
400 Bad Request: 요청 문법 오류 (클라이언트 잘못)
401 Unauthorized: 인증 안 됨 (로그인 필요)
403 Forbidden: 권한 없음 (로그인은 했지만 접근 불가)
404 Not Found: 요청한 리소스 없음 (없는 게시글/페이지)
500 Internal Server Error: 서버 내부 로직 에러 (코드 버그)
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception){
        ErrorResponse response = new ErrorResponse("BAD REQUEST", exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {

        // BindingResult에서 진짜 메세지 가져옴
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponse response = new ErrorResponse("BAD_REQUEST", errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}
