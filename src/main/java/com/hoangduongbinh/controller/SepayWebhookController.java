package com.hoangduongbinh.controller;

import com.hoangduongbinh.entity.Invoice;
import com.hoangduongbinh.repository.IInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/sepay")
public class SepayWebhookController {

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Value("${sepay.webhook.token}")
    private String sepayToken;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> payload) {

        // Log request
        System.out.println("[SePay] auth=[" + auth + "] payload=" + payload);
        // NOTE: SePay khong gui Authorization header nen bo check token

        try {
            Object amtObj = payload.get("transferAmount");
            if (amtObj == null) amtObj = payload.get("amountIn");
            if (amtObj == null) amtObj = payload.get("amount");
            if (amtObj == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "missing amount"));
            }
            double amount = Double.parseDouble(amtObj.toString());

            String content = (String) payload.get("content");
            if (content == null) content = (String) payload.get("transactionContent");
            if (content == null) content = (String) payload.get("description");
            System.out.println("[SePay] amount=" + amount + " content=" + content);
            if (content == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "missing content"));
            }

            Matcher m = Pattern.compile("PAY(\\d+)").matcher(content.toUpperCase());
            if (!m.find()) {
                System.err.println("[SePay] no PAY code in: " + content);
                return ResponseEntity.ok(Map.of("success", false, "message", "no PAY code"));
            }

            Long invoiceId = Long.parseLong(m.group(1));
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            if (invoice == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "not found"));
            }
            if (amount < invoice.getPrice()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "amount too low"));
            }

            invoice.setStatus("Da thanh toan");
            invoiceRepository.save(invoice);
            System.out.println("[SePay] paid invoice #" + invoiceId);
            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            System.err.println("[SePay error] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "error"));
        }
    }

    @GetMapping("/check-status/{invoiceId}")
    public ResponseEntity<?> checkStatus(@PathVariable("invoiceId") Long invoiceId) {
        Invoice inv = invoiceRepository.findById(invoiceId).orElse(null);
        if (inv == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("status", inv.getStatus()));
    }

    @PostMapping("/manual-confirm/{invoiceId}")
    public ResponseEntity<?> manualConfirm(@PathVariable("invoiceId") Long invoiceId) {
        Invoice inv = invoiceRepository.findById(invoiceId).orElse(null);
        if (inv == null) return ResponseEntity.notFound().build();
        inv.setStatus("Da thanh toan");
        invoiceRepository.save(inv);
        System.out.println("[Manual] confirmed #" + invoiceId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/qr-proxy")
    public ResponseEntity<byte[]> proxyQr(@RequestParam("url") String qrUrl) {
        try {
            URL u = new URL(qrUrl);
            try (InputStream in = u.openStream()) {
                byte[] bytes = in.readAllBytes();
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header("Cache-Control", "max-age=300")
                        .body(bytes);
            }
        } catch (Exception e) {
            System.err.println("[QR proxy error] " + e.getMessage());
            return ResponseEntity.status(502).build();
        }
    }
}