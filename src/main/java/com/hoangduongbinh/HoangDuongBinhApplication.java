package com.hoangduongbinh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Class khởi chạy chính của ứng dụng Spring Boot.
 * Annotation @SpringBootApplication bao gồm:
 * - @Configuration: Đánh dấu đây là class cấu hình
 * - @EnableAutoConfiguration: Tự động cấu hình Spring Boot
 * - @ComponentScan: Quét tất cả các component trong package com.hoangduongbinh
 */
@SpringBootApplication
public class HoangDuongBinhApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoangDuongBinhApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  ỨNG DỤNG THƯƠNG MẠI ĐIỆN TỬ ĐÃ KHỞI ĐỘNG  ");
        System.out.println("  Truy cập: http://localhost:8080              ");
        System.out.println("==============================================");
        System.out.println("==============================================");
    }

    /**
     * Script tự động chạy một lần khi khởi động Server để mở rộng cột image_url
     * thành kiểu TEXT, tránh lỗi DataIntegrityViolationException khi lưu quá dài.
     */
    @Bean
    public CommandLineRunner updateDatabaseSchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE book MODIFY COLUMN image_url TEXT");
                System.out.println(">> Đã cập nhật database: column image_url -> TEXT");
            } catch (Exception e) {
                // Nếu bảng chưa có hoặc lỗi khác thì log ra chứ không làm crash app
                System.out.println(">> Không thể ALTER TABLE: " + e.getMessage());
            }
        };
    }
}
