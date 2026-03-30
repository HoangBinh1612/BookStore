package com.hoangduongbinh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.repository.IInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Test cho SepayWebhookController.
 */
@WebMvcTest(SepayWebhookController.class)
class SepayWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IInvoiceRepository invoiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Invoice testInvoice;

    @BeforeEach
    void setUp() {
        testInvoice = Invoice.builder()
                .id(1L)
                .invoiceDate(new Date())
                .price(500000.0)
                .status("Chưa thanh toán")
                .build();
    }

    // ==================== POST /api/sepay/webhook ====================

    @Test
    @WithMockUser
    @DisplayName("POST /api/sepay/webhook - payment thành công")
    void handleWebhook_SuccessfulPayment_ShouldUpdateStatus() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transferAmount", 500000);
        payload.put("content", "Thanh toan PAY1 cho don hang");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        mockMvc.perform(post("/api/sepay/webhook")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/sepay/webhook - thiếu amount → error")
    void handleWebhook_MissingAmount_ShouldReturnError() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", "PAY1");

        mockMvc.perform(post("/api/sepay/webhook")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("missing amount"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/sepay/webhook - không có PAY code → error")
    void handleWebhook_NoPayCode_ShouldReturnError() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transferAmount", 500000);
        payload.put("content", "Random content without code");

        mockMvc.perform(post("/api/sepay/webhook")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("no PAY code"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/sepay/webhook - amount thấp hơn giá → error")
    void handleWebhook_AmountTooLow_ShouldReturnError() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transferAmount", 100000); // < 500000
        payload.put("content", "PAY1");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        mockMvc.perform(post("/api/sepay/webhook")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("amount too low"));
    }

    // ==================== GET /api/sepay/check-status/{id} ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/sepay/check-status/{id} → status")
    void checkStatus_WhenFound_ShouldReturnStatus() throws Exception {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        mockMvc.perform(get("/api/sepay/check-status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Chưa thanh toán"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/sepay/check-status/{id} - không tìm thấy → 404")
    void checkStatus_WhenNotFound_ShouldReturn404() throws Exception {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sepay/check-status/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/sepay/manual-confirm/{id} ====================

    @Test
    @WithMockUser
    @DisplayName("POST /api/sepay/manual-confirm/{id} → success")
    void manualConfirm_WhenFound_ShouldConfirm() throws Exception {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

        mockMvc.perform(post("/api/sepay/manual-confirm/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/sepay/manual-confirm/{id} - không tìm thấy → 404")
    void manualConfirm_WhenNotFound_ShouldReturn404() throws Exception {
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/sepay/manual-confirm/999").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
