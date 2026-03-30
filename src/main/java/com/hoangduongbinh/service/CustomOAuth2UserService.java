package com.hoangduongbinh.service;

import com.hoangduongbinh.entity.Role;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.IRoleRepository;
import com.hoangduongbinh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service xử lý đăng nhập bằng Google OAuth2.
 *
 * Luồng hoạt động:
 * 1. User nhấn "Đăng nhập bằng Google"
 * 2. Google trả về thông tin user (email, name)
 * 3. Service kiểm tra email đã có trong DB chưa
 * - Nếu chưa: tạo User mới với provider="google", gán role USER
 * - Nếu rồi: cập nhật provider="google"
 * 4. Trả về OAuth2User cho Spring Security xác thực
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Tìm hoặc tạo user từ Google
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setProvider("google");
            userRepository.save(user);
        } else {
            // Tạo user mới từ Google
            user = new User();
            user.setEmail(email);
            user.setUsername(email); // Dùng email làm username
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
            user.setProvider("google");

            // Gán role USER mặc định
            Role userRole = roleRepository.findByName("USER");
            if (userRole != null) {
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                user.setRoles(roles);
            }

            userRepository.save(user);
        }

        // Chuyển roles thành authorities cho Spring Security
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                "email" // Dùng email làm key chính
        );
    }
}
