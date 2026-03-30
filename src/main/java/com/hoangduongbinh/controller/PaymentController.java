package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.repository.IInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý màn hình Thanh toán qua QR Code (VietQR / SePay).
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Value("${vietqr.bank.bin}")
    private String bankBin;

    @Value("${vietqr.bank.account}")
    private String bankAccount;

    @Value("${vietqr.bank.accountName}")
    private String bankAccountName;

    @Value("${vietqr.template}")
    private String qrTemplate;

    @GetMapping("/qr/{invoiceId}")
    public String showPaymentQr(@PathVariable("invoiceId") Long invoiceId, Model model) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);

        if (invoice == null) {
            return "redirect:/"; // Nếu không tìm thấy hóa đơn, về trang chủ
        }

        // Tự động generate VietQR URL theo định dạng chuẩn của vietqr.io
        // Định dạng URL:
        // https://img.vietqr.io/image/<BANK_BIN>-<ACCOUNT_NO>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<DESCRIPTION>&accountName=<ACCOUNT_NAME>

        long amount = (long) invoice.getPrice();
        String description = "PAY" + invoice.getId(); // Mã thanh toán mặc định PAY + ID

        String qrUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                bankBin, bankAccount, qrTemplate, amount, description, bankAccountName.replace(" ", "%20"));

        // Proxy URL: frontend tải QR qua backend để tránh lỗi CORS
        String qrProxyUrl = "/api/sepay/qr-proxy?url="
                + java.net.URLEncoder.encode(qrUrl, java.nio.charset.StandardCharsets.UTF_8);

        model.addAttribute("invoice", invoice);
        model.addAttribute("qrUrl", qrProxyUrl);
        model.addAttribute("bankBin", bankBin);
        model.addAttribute("bankAccount", bankAccount);
        model.addAttribute("bankAccountName", bankAccountName);
        model.addAttribute("description", description);

        return "payment-qr";
    }

    @PostMapping("/cancel/{invoiceId}")
    public String cancelOrder(@PathVariable("invoiceId") Long invoiceId, RedirectAttributes redirectAttributes) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);

        if (invoice != null && "Chưa thanh toán".equals(invoice.getStatus())) {
            invoice.setStatus("Đã hủy");
            invoiceRepository.save(invoice);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể hủy đơn hàng này hoặc đơn hàng đã được xử lý!");
        }

        return "redirect:/orders";
    }
}
