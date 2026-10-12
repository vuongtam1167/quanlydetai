package com.example.quanlydetai.config;

import com.example.quanlydetai.model.enums.Role;
import com.example.quanlydetai.model.User;
import com.example.quanlydetai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Kích hoạt các annotation bảo mật cấp method như @PreAuthorize trên controllers
public class SecurityConfig {

    // Sử dụng BCryptPasswordEncoder để mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tự động chèn tài khoản admin mặc định vào MySQL với mật khẩu đã mã hóa nếu bảng users đang trống
    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                userRepository.save(admin);
                
                User truongKhoa = new User();
                truongKhoa.setUsername("truongkhoa");
                truongKhoa.setPassword(encoder.encode("khoa123"));
                truongKhoa.setRole(Role.TRUONG_KHOA);
                truongKhoa.setEnabled(true);
                userRepository.save(truongKhoa);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/v1/**") // Tắt CSRF cho các REST endpoints để các công cụ như Postman có thể gọi POST, PUT, DELETE
            )
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập tự do vào các tài nguyên tĩnh và trang đăng nhập
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
                
                // Phân quyền API theo yêu cầu nghiệp vụ
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/**").hasAnyRole("ADMIN", "TRUONG_KHOA")
                .requestMatchers("/api/v1/giangvien/**").hasRole("GIANG_VIEN")
                .requestMatchers("/api/v1/sinhvien/**").hasRole("SINH_VIEN")
                
                // Yêu cầu xác thực cho tất cả các request bắt đầu bằng /api/v1/ và /topics/
                .requestMatchers("/api/v1/**").authenticated()
                .requestMatchers("/topics/**").authenticated()
                
                // Các request khác đều được phép đi qua (có thể điều chỉnh lại tùy nghiệp vụ)
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .defaultSuccessUrl("/", true) // Chuyển hướng về trang chủ sau khi đăng nhập thành công
                .permitAll()
            )
            .httpBasic(Customizer.withDefaults()) // Kích hoạt xác thực HTTP Basic cho các REST API clients
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout") // Trở về trang đăng nhập sau khi đăng xuất
                .permitAll()
            );

        return http.build();
    }
}