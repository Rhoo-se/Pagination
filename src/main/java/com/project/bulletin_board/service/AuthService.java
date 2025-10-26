package com.project.bulletin_board.service;


import com.project.bulletin_board.dto.user.LoginRequest;
import com.project.bulletin_board.dto.user.UserSignUpRequest;
import com.project.bulletin_board.entity.User;
import com.project.bulletin_board.jwt.JwtTokenProvider;
import com.project.bulletin_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(UserSignUpRequest userSignUpRequest){
        if(userRepository.existsByEmail(userSignUpRequest.getEmail())){
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        if(userRepository.existsByNickname(userSignUpRequest.getNickname())){
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(userSignUpRequest.getPassword());

        User user = User.builder()
                .email(userSignUpRequest.getEmail())
                .password(encodedPassword)
                .nickname(userSignUpRequest.getNickname())
                .build();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest loginRequest){

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일 입니다."));
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        String token = jwtTokenProvider.createToken(user.getEmail());
        return token;
    }
}
