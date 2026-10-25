package com.techpulse.wallet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.techpulse.wallet.dto.TransactionHistoryResponse;

import com.techpulse.wallet.dto.TransferResponse;
import java.util.Comparator;
import java.util.List;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techpulse.wallet.dto.AddMoneyResponse;
import com.techpulse.wallet.dto.WalletResponse;
import com.techpulse.wallet.entity.User;
import com.techpulse.wallet.entity.Wallet;
import com.techpulse.wallet.entity.WalletTransaction;
import com.techpulse.wallet.enums.TransactionStatus;
import com.techpulse.wallet.enums.TransactionType;
import com.techpulse.wallet.repository.UserRepository;
import com.techpulse.wallet.repository.WalletRepository;
import com.techpulse.wallet.repository.WalletTransactionRepository;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(String email) {
        User user = userRepository.findByEmail(
                email.trim().toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        return new WalletResponse(
                wallet.getId(),
                user.getEmail(),
                wallet.getBalance());
    }

    @Transactional
    public AddMoneyResponse addMoney(
            String email,
            BigDecimal amount,
            String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "A valid Idempotency-Key header is required");
        }

        if (amount == null
                || amount.compareTo(new BigDecimal("0.01")) < 0
                || amount.scale() > 2
                || amount.precision() > 19) {
            throw new IllegalArgumentException(
                    "Amount must be positive with at most 2 decimal places");
        }

        var existing = transactionRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            WalletTransaction tx = existing.get();

            if (tx.getType() != TransactionType.ADD
                    || tx.getToWallet() == null
                    || !tx.getToWallet().getUser().getEmail()
                            .equalsIgnoreCase(email)
                    || tx.getAmount().compareTo(amount) != 0) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used for another request");
            }

            return new AddMoneyResponse(
                    tx.getId(),
                    tx.getAmount(),
                    tx.getToWallet().getBalance(),
                    tx.getStatus().name(),
                    "Request already processed");
        }

        User user = userRepository.findByEmail(
                email.trim().toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Wallet wallet = walletRepository
                .findByUserIdForUpdate(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.saveAndFlush(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setToWallet(wallet);
        tx.setAmount(amount);
        tx.setType(TransactionType.ADD);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setIdempotencyKey(idempotencyKey);

        WalletTransaction savedTx =
                transactionRepository.saveAndFlush(tx);

        return new AddMoneyResponse(
                savedTx.getId(),
                amount,
                wallet.getBalance(),
                savedTx.getStatus().name(),
                "Money added successfully");
    }
    

@Transactional
public TransferResponse transfer(
        String senderEmail,
        String toEmail,
        BigDecimal amount,
        String idempotencyKey) {

    if (idempotencyKey == null || idempotencyKey.isBlank()
            || idempotencyKey.length() > 100) {
        throw new IllegalArgumentException(
                "A valid Idempotency-Key header is required");
    }

    if (amount == null
            || amount.compareTo(new BigDecimal("0.01")) < 0
            || amount.scale() > 2
            || amount.precision() > 19) {
        throw new IllegalArgumentException(
                "Amount must be positive with at most 2 decimal places");
    }

    senderEmail = senderEmail.trim().toLowerCase();
    toEmail = toEmail.trim().toLowerCase();

    if (senderEmail.equals(toEmail)) {
        throw new IllegalArgumentException(
                "You cannot transfer money to your own wallet");
    }

    // Check whether this request was already processed.
    var existing = transactionRepository
            .findByIdempotencyKey(idempotencyKey);

    if (existing.isPresent()) {
        WalletTransaction tx = existing.get();

        if (tx.getType() != TransactionType.TRANSFER
                || tx.getFromWallet() == null
                || tx.getToWallet() == null
                || !tx.getFromWallet().getUser().getEmail()
                        .equalsIgnoreCase(senderEmail)
                || !tx.getToWallet().getUser().getEmail()
                        .equalsIgnoreCase(toEmail)
                || tx.getAmount().compareTo(amount) != 0) {
            throw new IllegalArgumentException(
                    "Idempotency key was already used for another request");
        }

        return new TransferResponse(
                tx.getId(),
                senderEmail,
                toEmail,
                tx.getAmount(),
                tx.getFromWallet().getBalance(),
                tx.getStatus().name(),
                "Request already processed");
    }

    User sender = userRepository.findByEmail(senderEmail)
            .orElseThrow(() ->
                    new IllegalArgumentException("Sender not found"));

    User recipient = userRepository.findByEmail(toEmail)
            .orElseThrow(() ->
                    new IllegalArgumentException("Recipient not found"));

    // Lock wallets in a consistent order to reduce deadlock risk.
    List<Long> userIds = List.of(sender.getId(), recipient.getId())
            .stream()
            .sorted(Comparator.naturalOrder())
            .toList();

    Wallet firstWallet = walletRepository
            .findByUserIdForUpdate(userIds.get(0))
            .orElseThrow(() ->
                    new IllegalArgumentException("Wallet not found"));

    Wallet secondWallet = walletRepository
            .findByUserIdForUpdate(userIds.get(1))
            .orElseThrow(() ->
                    new IllegalArgumentException("Wallet not found"));

    Wallet senderWallet =
            firstWallet.getUser().getId().equals(sender.getId())
                    ? firstWallet : secondWallet;

    Wallet recipientWallet =
            firstWallet.getUser().getId().equals(recipient.getId())
                    ? firstWallet : secondWallet;

    if (senderWallet.getBalance().compareTo(amount) < 0) {
        throw new IllegalArgumentException("Insufficient wallet balance");
    }

    senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
    recipientWallet.setBalance(recipientWallet.getBalance().add(amount));

    walletRepository.saveAndFlush(senderWallet);
    walletRepository.saveAndFlush(recipientWallet);

    WalletTransaction tx = new WalletTransaction();
    tx.setFromWallet(senderWallet);
    tx.setToWallet(recipientWallet);
    tx.setAmount(amount);
    tx.setType(TransactionType.TRANSFER);
    tx.setStatus(TransactionStatus.SUCCESS);
    tx.setIdempotencyKey(idempotencyKey);

    WalletTransaction savedTx =
            transactionRepository.saveAndFlush(tx);

    return new TransferResponse(
            savedTx.getId(),
            senderEmail,
            toEmail,
            amount,
            senderWallet.getBalance(),
            savedTx.getStatus().name(),
            "Money transferred successfully");
}



public Page<TransactionHistoryResponse> getTransactionHistory(
        String email, int page, int size) {

    if (page < 0) {
        throw new IllegalArgumentException(
                "Page number cannot be negative");
    }

    if (size < 1 || size > 100) {
        throw new IllegalArgumentException(
                "Page size must be between 1 and 100");
    }

    Pageable pageable = PageRequest.of(
            page, size, Sort.by("createdAt").descending());

    Page<com.techpulse.wallet.entity.WalletTransaction> transactions =
            transactionRepository
                    .findByFromWalletUserEmailOrToWalletUserEmail(
                            email, email, pageable);

    return transactions.map(tx ->
            new TransactionHistoryResponse(
                    tx.getId(),
                    tx.getFromWallet() == null ? null
                            : tx.getFromWallet().getUser().getEmail(),
                    tx.getToWallet() == null ? null
                            : tx.getToWallet().getUser().getEmail(),
                    tx.getAmount(),
                    tx.getType().name(),
                    tx.getStatus().name(),
                    tx.getCreatedAt()));
}


}
