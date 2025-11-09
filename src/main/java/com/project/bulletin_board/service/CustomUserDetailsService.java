package com.project.bulletin_board.service;
import com.project.bulletin_board.entity.User;
import com.project.bulletin_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // 1. import

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 2. 님이 만든 UserRepository.findByEmail()을 사용
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다: " + email));

        // 3. Spring Security의 User 객체로 변환하여 반환
        // (권한(Role)이 지금은 없으므로 빈 리스트(ArrayList)를 넘겨줍니다)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>() // 권한(Roles) 리스트
        );
    }
}