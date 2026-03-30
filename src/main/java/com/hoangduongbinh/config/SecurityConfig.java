package com.hoangduongbinh.config;

import com.hoangduongbinh.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Spring Security - Bảo mật ứng dụng.
 *
 * Hỗ trợ 2 cách đăng nhập:
 * 1. Form login (username/password)
 * 2. OAuth2 Google login
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomOAuth2UserService customOAuth2UserService;

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/**"))

                                .authorizeHttpRequests(auth -> auth
                                                // Trang công khai
                                                .requestMatchers(
                                                                "/", "/home",
                                                                "/books/**",
                                                                "/login", "/register",
                                                                "/css/**", "/js/**", "/images/**",
                                                                "/payment/**", "/api/sepay/**")
                                                .permitAll()

                                                // Trang Admin - chỉ ADMIN
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                // Tất cả còn lại - phải đăng nhập
                                                .anyRequest().authenticated())

                                // === Form đăng nhập (username/password) ===
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/login?error=true")
                                                .permitAll())

                                // === OAuth2 đăng nhập (Google) ===
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .defaultSuccessUrl("/", true)
                                                .failureUrl("/login?error=true"))

                                // === Đăng xuất ===
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

                return http.build();
        }
}
