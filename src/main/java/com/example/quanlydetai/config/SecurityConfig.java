package com.example.quanlydetai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Bảo mật nhiều lớp:
 *  1) Lớp URL (file này): mỗi nhóm đường dẫn chỉ cho đúng vai trò.
 *  2) Lớp method: @PreAuthorize trên từng controller.
 * Mật khẩu luôn được mã hóa BCrypt. Tài khoản mẫu do DataSeeder tạo.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico",
                                 "/login", "/error").permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers("/department/**").hasRole("TRUONG_BO_MON")
                .requestMatchers("/lecturer/**").hasAnyRole("GIANG_VIEN", "TRUONG_BO_MON")
                .requestMatchers("/student/**").hasRole("SINH_VIEN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .httpBasic(Customizer.withDefaults()) // cho REST client (Postman) ở /api/v1/**
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}
