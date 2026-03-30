package com.hoangduongbinh.viewmodel;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * ViewModel nhận dữ liệu sách từ REST API (POST/PUT request).
 * Dùng để validate dữ liệu đầu vào trước khi tạo/sửa sách.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookPostVm {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 1, max = 50, message = "Tiêu đề phải từ 1-50 ký tự")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    @Size(min = 1, max = 50, message = "Tác giả phải từ 1-50 ký tự")
    private String author;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double price;

    private Long categoryId;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stock;
}
