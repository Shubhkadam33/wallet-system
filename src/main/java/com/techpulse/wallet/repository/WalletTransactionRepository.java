package com.techpulse.wallet.repository;




import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.techpulse.wallet.entity.WalletTransaction;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    Optional<WalletTransaction> findByIdempotencyKey(
            String idempotencyKey);

    Page<WalletTransaction>
        findByFromWalletUserEmailOrToWalletUserEmail(
            String fromEmail,
            String toEmail,
            Pageable pageable);
}

