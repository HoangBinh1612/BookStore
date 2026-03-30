package com.hoangduongbinh.service;

import com.hoangduongbinh.dto.RegisterDTO;
import com.hoangduongbinh.entity.Role;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.IRoleRepository;
import com.hoangduongbinh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Service xử lý logic nghiệp vụ liên quan đến User.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Đăng ký tài khoản mới.
     * Gán role mặc định là "USER".
     */
    public User registerNewUser(RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setProvider("local");

        // Gán role USER mặc định
        Role userRole = roleRepository.findByName("USER");
        if (userRole != null) {
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Cập nhật thông tin cá nhân (email, phone).
     */
    public void updateProfile(String username, String email, String phone) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác!");
        }

        user.setEmail(email);
        user.setPhone(phone);
        userRepository.save(user);
    }

    /**
     * Đổi mật khẩu (kiểm tra mật khẩu cũ trước).
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
