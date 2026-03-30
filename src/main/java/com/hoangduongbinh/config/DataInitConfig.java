package com.hoangduongbinh.config;

import com.hoangduongbinh.entity.Book;
import com.hoangduongbinh.entity.Category;
import com.hoangduongbinh.entity.Role;
import com.hoangduongbinh.entity.User;
import com.hoangduongbinh.repository.CategoryRepository;
import com.hoangduongbinh.repository.IBookRepository;
import com.hoangduongbinh.repository.IRoleRepository;
import com.hoangduongbinh.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

/**
 * Cấu hình khởi tạo dữ liệu mẫu khi ứng dụng chạy lần đầu.
 */
@Configuration
public class DataInitConfig {

        @Bean
        public CommandLineRunner initData(UserRepository userRepository,
                        CategoryRepository categoryRepository,
                        IBookRepository bookRepository,
                        IRoleRepository roleRepository,
                        PasswordEncoder passwordEncoder) {
                return args -> {
                        // === 1. Tạo Role mặc định ===
                        if (roleRepository.count() == 0) {
                                roleRepository.save(Role.builder().name("ADMIN").build());
                                roleRepository.save(Role.builder().name("USER").build());
                                System.out.println(">>> Đã tạo 2 role: ADMIN, USER");
                        }

                        // === 2. Tạo tài khoản Admin mặc định ===
                        if (!userRepository.existsByUsername("admin")) {
                                User admin = new User();
                                admin.setUsername("admin");
                                admin.setPassword(passwordEncoder.encode("admin123"));
                                admin.setEmail("admin@bookstore.com");
                                admin.setProvider("local");

                                // Gán cả 2 role cho admin
                                Set<Role> adminRoles = new HashSet<>();
                                adminRoles.add(roleRepository.findByName("ADMIN"));
                                adminRoles.add(roleRepository.findByName("USER"));
                                admin.setRoles(adminRoles);

                                userRepository.save(admin);
                                System.out.println(">>> Đã tạo tài khoản ADMIN: admin / admin123");
                        }

                        // === 3. Tạo danh mục sách + sách mẫu ===
                        if (categoryRepository.count() == 0) {
                                Category congNghe = new Category();
                                congNghe.setName("Công nghệ thông tin");
                                Category vanHoc = new Category();
                                vanHoc.setName("Văn học");
                                Category khoaHoc = new Category();
                                khoaHoc.setName("Khoa học");
                                Category kinhTe = new Category();
                                kinhTe.setName("Kinh tế");
                                Category ngoaiNgu = new Category();
                                ngoaiNgu.setName("Ngoại ngữ");

                                categoryRepository.save(congNghe);
                                categoryRepository.save(vanHoc);
                                categoryRepository.save(khoaHoc);
                                categoryRepository.save(kinhTe);
                                categoryRepository.save(ngoaiNgu);
                                System.out.println(">>> Đã tạo 5 danh mục sách mẫu");

                                String[][] booksData = {
                                                { "Spring Boot in Action", "Craig Walls", "450000", "1" },
                                                { "Java: The Complete Reference", "Herbert Schildt", "520000", "1" },
                                                { "Clean Code", "Robert C. Martin", "380000", "1" },
                                                { "Head First Design Patterns", "Eric Freeman", "420000", "1" },
                                                { "Truyện Kiều", "Nguyễn Du", "85000", "2" },
                                                { "Số Đỏ", "Vũ Trọng Phụng", "65000", "2" },
                                                { "Nhà Giả Kim", "Paulo Coelho", "79000", "2" },
                                                { "Sapiens: Lược sử loài người", "Yuval Noah Harari", "199000", "3" },
                                                { "Vũ trụ trong vỏ hạt dẻ", "Stephen Hawking", "165000", "3" },
                                                { "Kinh tế học vĩ mô", "N. Gregory Mankiw", "250000", "4" },
                                                { "Cha giàu cha nghèo", "Robert Kiyosaki", "110000", "4" },
                                                { "English Grammar in Use", "Raymond Murphy", "195000", "5" },
                                };

                                for (String[] data : booksData) {
                                        Book book = new Book();
                                        book.setTitle(data[0]);
                                        book.setAuthor(data[1]);
                                        book.setPrice(Double.parseDouble(data[2]));
                                        book.setCategory(categoryRepository.findById(Long.parseLong(data[3]))
                                                        .orElse(null));
                                        book.setStock((int) (Math.random() * 41) + 10); // Random stock from 10 to 50
                                        bookRepository.save(book);
                                }
                                System.out.println(">>> Đã tạo " + booksData.length + " sách mẫu");
                        }
                };
        }
}
