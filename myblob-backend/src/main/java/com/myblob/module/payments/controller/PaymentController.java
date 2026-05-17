package com.myblob.module.payments.controller;

import com.myblob.common.ApiResponse;
import com.myblob.common.PageResponse;
import com.myblob.module.payments.dto.OrderDTO;
import com.myblob.module.payments.dto.WalletDTO;
import com.myblob.module.payments.dto.WalletTransactionDTO;
import com.myblob.module.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/orders/")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getMyOrders(page, size)));
    }

    @PostMapping("/orders/")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@RequestBody Map<String, Long> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(paymentService.createOrder(body.get("plan_id"))));
    }

    @PostMapping("/orders/{id}/pay/")
    public ResponseEntity<ApiResponse<OrderDTO>> payOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.payOrder(id, body.getOrDefault("payment_method", "alipay"))));
    }

    @PostMapping("/orders/{id}/cancel/")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.cancelOrder(id)));
    }

    @GetMapping("/wallet/my-wallet/")
    public ResponseEntity<ApiResponse<WalletDTO>> getMyWallet() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getMyWallet()));
    }

    @GetMapping("/wallet/transactions/")
    public ResponseEntity<ApiResponse<PageResponse<WalletTransactionDTO>>> getMyWalletTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getMyWalletTransactions(page, size)));
    }
}
