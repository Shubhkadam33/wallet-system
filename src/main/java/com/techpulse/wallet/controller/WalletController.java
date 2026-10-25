package com.techpulse.wallet.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import com.techpulse.wallet.dto.TransactionHistoryResponse;

import com.techpulse.wallet.dto.TransferRequest;
import com.techpulse.wallet.dto.TransferResponse;

import java.math.BigDecimal;

import com.techpulse.wallet.dto.AddMoneyRequest;
import com.techpulse.wallet.dto.AddMoneyResponse;
import com.techpulse.wallet.dto.WalletResponse;
import com.techpulse.wallet.service.WalletService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(
            Authentication authentication) {

        WalletResponse response =
                walletService.getWallet(authentication.getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<AddMoneyResponse> addMoney(
            Authentication authentication,
            @Valid @RequestBody AddMoneyRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        AddMoneyResponse response = walletService.addMoney(
                authentication.getName(),
                request.getAmount(),
                idempotencyKey);

        return ResponseEntity.ok(response);
    }
    

@PostMapping("/transfer")
public ResponseEntity<TransferResponse> transfer(
        Authentication authentication,
        @Valid @RequestBody TransferRequest request,
        @RequestHeader("Idempotency-Key") String idempotencyKey) {

    TransferResponse response = walletService.transfer(
            authentication.getName(),
            request.getToEmail(),
            request.getAmount(),
            idempotencyKey);

    return ResponseEntity.ok(response);
}



@GetMapping("/transactions")
public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
        Authentication authentication,
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Page must be zero or greater")
        int page,

        @RequestParam(defaultValue = "10")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100")
        int size) {

    Page<TransactionHistoryResponse> response =
            walletService.getTransactionHistory(
                    authentication.getName(), page, size);

    return ResponseEntity.ok(response);
}



}
