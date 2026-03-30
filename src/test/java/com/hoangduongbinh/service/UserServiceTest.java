package com.hoangduongbinh.service;

import com.hoangduongbinh.dto.RegisterDTO;
import com.hoangduongbinh.entity.Role;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.IRoleRepository;
import com.hoangduongbinh.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@email.com");
        testUser.setPhone("0123456789");
        testUser.setProvider("local");
    }

    // ==================== registerNewUser ====================

    @Test
    @DisplayName("registerNewUser - tạo user mới gán role USER")
    void registerNewUser_ShouldCreateUserWithUserRole() {
        RegisterDTO dto = new RegisterDTO("newuser", "new@email.com", "password123", "password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(roleRepository.findByName("USER")).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        User result = userService.registerNewUser(dto);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@email.com", result.getEmail());
        assertEquals("encodedPass", result.getPassword());
        assertEquals("local", result.getProvider());
        assertTrue(result.getRoles().contains(userRole));
        verify(userRepository).save(any(User.class));
    }

    // ==================== isUsernameExists ====================

    @Test
    @DisplayName("isUsernameExists - username đã tồn tại → true")
    void isUsernameExists_WhenExists_ShouldReturnTrue() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertTrue(userService.isUsernameExists("testuser"));
    }

    @Test
    @DisplayName("isUsernameExists - username chưa tồn tại → false")
    void isUsernameExists_WhenNotExists_ShouldReturnFalse() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        assertFalse(userService.isUsernameExists("newuser"));
    }

    // ==================== isEmailExists ====================

    @Test
    @DisplayName("isEmailExists - email đã tồn tại → true")
    void isEmailExists_WhenExists_ShouldReturnTrue() {
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        assertTrue(userService.isEmailExists("test@email.com"));
    }

    @Test
    @DisplayName("isEmailExists - email chưa tồn tại → false")
    void isEmailExists_WhenNotExists_ShouldReturnFalse() {
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);

        assertFalse(userService.isEmailExists("new@email.com"));
    }

    // ==================== findByUsername ====================

    @Test
    @DisplayName("findByUsername - tìm thấy → trả về user")
    void findByUsername_WhenFound_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("findByUsername - không tìm thấy → null")
    void findByUsername_WhenNotFound_ShouldReturnNull() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        User result = userService.findByUsername("nonexistent");

        assertNull(result);
    }

    // ==================== updateProfile ====================

    @Test
    @DisplayName("updateProfile - cập nhật email + phone thành công")
    void updateProfile_ShouldUpdateSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@email.com")).thenReturn(false);

        userService.updateProfile("testuser", "newemail@email.com", "0987654321");

        assertEquals("newemail@email.com", testUser.getEmail());
        assertEquals("0987654321", testUser.getPhone());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("updateProfile - email trùng user khác → RuntimeException")
    void updateProfile_DuplicateEmail_ShouldThrowException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("other@email.com")).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> userService.updateProfile("testuser", "other@email.com", "0123"));
    }

    @Test
    @DisplayName("updateProfile - user không tồn tại → RuntimeException")
    void updateProfile_UserNotFound_ShouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateProfile("nonexistent", "a@b.com", "0123"));
    }

    // ==================== changePassword ====================

    @Test
    @DisplayName("changePassword - đúng mật khẩu cũ → đổi thành công")
    void changePassword_CorrectOldPassword_ShouldChange() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncodedPass");

        userService.changePassword("testuser", "oldPass", "newPass");

        assertEquals("newEncodedPass", testUser.getPassword());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("changePassword - sai mật khẩu cũ → RuntimeException")
    void changePassword_WrongOldPassword_ShouldThrowException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> userService.changePassword("testuser", "wrongPass", "newPass"));
    }
}
