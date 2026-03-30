package com.hoangduongbinh.viewmodel;

import lombok.*;

/**
 * ViewModel trả về thông tin sách cho REST API (GET response).
 * Không chứa thông tin nhạy cảm hay entity phức tạp.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookGetVm {
    private Long id;
    private String title;
    private String author;
    private Double price;
    private Long categoryId;
    private String categoryName;
    private Integer stock;
}
