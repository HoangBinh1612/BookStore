package com.hoangduongbinh.service;

import com.hoangduongbinh.entity.Role;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho CustomUserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        Role userRole = Role.builder().id(2L).name("USER").build();

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("admin@test.com");
        testUser.setRoles(roles);
    }

    @Test
    @DisplayName("loadUserByUsername - tìm thấy → UserDetails với ROLE_ prefix")
    void loadUserByUsername_WhenFound_ShouldReturnUserDetailsWithRolePrefix() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(2, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("loadUserByUsername - không tìm thấy → UsernameNotFoundException")
    void loadUserByUsername_WhenNotFound_ShouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent"));
    }
}
