package com.techpulse.wallet.controller;

import com.techpulse.wallet.dto.AdminWalletResponse;
import com.techpulse.wallet.entity.Wallet;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techpulse.wallet.dto.TransactionHistoryResponse;
import com.techpulse.wallet.entity.WalletTransaction;
import com.techpulse.wallet.repository.WalletRepository;
import com.techpulse.wallet.repository.WalletTransactionRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public AdminController(
            WalletRepository walletRepository,
            WalletTransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionHistoryResponse>>
            getAllTransactions() {

        List<TransactionHistoryResponse> response =
                transactionRepository.findAll()
                        .stream()
                        .map(tx -> new TransactionHistoryResponse(
                                tx.getId(),
                                tx.getFromWallet() == null ? null
                                        : tx.getFromWallet().getUser().getEmail(),
                                tx.getToWallet() == null ? null
                                        : tx.getToWallet().getUser().getEmail(),
                                tx.getAmount(),
                                tx.getType().name(),
                                tx.getStatus().name(),
                                tx.getCreatedAt()))
                        .toList();

        return ResponseEntity.ok(response);
    }
    

@GetMapping("/wallets")
public ResponseEntity<List<AdminWalletResponse>> getAllWallets() {

    List<AdminWalletResponse> response = walletRepository.findAll()
            .stream()
            .map(wallet -> new AdminWalletResponse(
                    wallet.getId(),
                    wallet.getUser().getEmail(),
                    wallet.getBalance()))
            .toList();

    return ResponseEntity.ok(response);
}

}
