package com.hoangduongbinh.service;

import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service xác thực người dùng cho Spring Security.
 * Load user từ DB → chuyển thành UserDetails cho Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Không tìm thấy tài khoản: " + username));

                // Chuyển Set<Role> → List<SimpleGrantedAuthority>
                // Mỗi role name thêm prefix "ROLE_" cho Spring Security
                return new org.springframework.security.core.userdetails.User(
                                user.getUsername(),
                                user.getPassword(),
                                user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                                                .collect(Collectors.toList()));
        }
}
