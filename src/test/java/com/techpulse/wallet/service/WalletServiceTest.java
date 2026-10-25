package com.techpulse.wallet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import com.techpulse.wallet.dto.TransactionHistoryResponse;

import com.techpulse.wallet.dto.TransferResponse;

import com.techpulse.wallet.dto.AddMoneyResponse;
import com.techpulse.wallet.dto.WalletResponse;
import com.techpulse.wallet.entity.User;
import com.techpulse.wallet.entity.Wallet;
import com.techpulse.wallet.entity.WalletTransaction;
import com.techpulse.wallet.enums.Role;
import com.techpulse.wallet.enums.TransactionStatus;
import com.techpulse.wallet.enums.TransactionType;
import com.techpulse.wallet.repository.UserRepository;
import com.techpulse.wallet.repository.WalletRepository;
import com.techpulse.wallet.repository.WalletTransactionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        return user;
    }

    private Wallet createWallet(Long id, User user, String balance) {
        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal(balance));
        return wallet;
    }

    @Test
    void getWalletShouldReturnWalletDetails() {
        User user = createUser(1L, "test@example.com");
        Wallet wallet = createWallet(10L, user, "100.00");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L))
                .thenReturn(Optional.of(wallet));

        WalletResponse response =
                walletService.getWallet(" TEST@example.com ");

        assertEquals("test@example.com", response.getEmail());
        assertEquals(0,
                response.getBalance().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void getWalletShouldRejectUnknownUser() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.getWallet("missing@example.com"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void addMoneyShouldIncreaseBalance() {
        User user = createUser(1L, "test@example.com");
        Wallet wallet = createWallet(10L, user, "100.00");

        when(transactionRepository.findByIdempotencyKey("add-test-001"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository.saveAndFlush(any(WalletTransaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        AddMoneyResponse response = walletService.addMoney(
                "test@example.com",
                new BigDecimal("50.00"),
                "add-test-001");

        assertEquals(0,
                wallet.getBalance().compareTo(new BigDecimal("150.00")));
        assertEquals(0,
                response.getBalance().compareTo(new BigDecimal("150.00")));
        assertEquals("SUCCESS", response.getStatus());

        verify(walletRepository).saveAndFlush(wallet);
        verify(transactionRepository)
                .saveAndFlush(any(WalletTransaction.class));
    }

    @Test
    void addMoneyShouldRejectInvalidIdempotencyKey() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.addMoney(
                        "test@example.com",
                        new BigDecimal("50.00"),
                        " "));

        assertEquals(
                "A valid Idempotency-Key header is required",
                exception.getMessage());

        verifyNoInteractions(userRepository, walletRepository,
                transactionRepository);
    }
    

@Test
void transferShouldMoveMoneyBetweenWallets() {
    User sender = createUser(1L, "sender@example.com");
    User recipient = createUser(2L, "recipient@example.com");

    Wallet senderWallet = createWallet(10L, sender, "200.00");
    Wallet recipientWallet = createWallet(20L, recipient, "50.00");

    when(transactionRepository.findByIdempotencyKey("transfer-test-001"))
            .thenReturn(Optional.empty());

    when(userRepository.findByEmail("sender@example.com"))
            .thenReturn(Optional.of(sender));
    when(userRepository.findByEmail("recipient@example.com"))
            .thenReturn(Optional.of(recipient));

    when(walletRepository.findByUserIdForUpdate(1L))
            .thenReturn(Optional.of(senderWallet));
    when(walletRepository.findByUserIdForUpdate(2L))
            .thenReturn(Optional.of(recipientWallet));

    when(transactionRepository.saveAndFlush(any(WalletTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    TransferResponse response = walletService.transfer(
            "sender@example.com",
            "recipient@example.com",
            new BigDecimal("75.00"),
            "transfer-test-001");

    assertEquals(0,
            senderWallet.getBalance().compareTo(new BigDecimal("125.00")));
    assertEquals(0,
            recipientWallet.getBalance().compareTo(new BigDecimal("125.00")));
    assertEquals("SUCCESS", response.getStatus());

    verify(walletRepository).saveAndFlush(senderWallet);
    verify(walletRepository).saveAndFlush(recipientWallet);
    verify(transactionRepository)
            .saveAndFlush(any(WalletTransaction.class));
}


@Test
void transferShouldRejectInsufficientBalance() {
    User sender = createUser(1L, "sender@example.com");
    User recipient = createUser(2L, "recipient@example.com");

    Wallet senderWallet = createWallet(10L, sender, "20.00");
    Wallet recipientWallet = createWallet(20L, recipient, "50.00");

    when(transactionRepository.findByIdempotencyKey("transfer-test-002"))
            .thenReturn(Optional.empty());

    when(userRepository.findByEmail("sender@example.com"))
            .thenReturn(Optional.of(sender));
    when(userRepository.findByEmail("recipient@example.com"))
            .thenReturn(Optional.of(recipient));

    when(walletRepository.findByUserIdForUpdate(1L))
            .thenReturn(Optional.of(senderWallet));
    when(walletRepository.findByUserIdForUpdate(2L))
            .thenReturn(Optional.of(recipientWallet));

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> walletService.transfer(
                    "sender@example.com",
                    "recipient@example.com",
                    new BigDecimal("100.00"),
                    "transfer-test-002"));

    assertEquals("Insufficient wallet balance", exception.getMessage());

    assertEquals(0,
            senderWallet.getBalance().compareTo(new BigDecimal("20.00")));
    assertEquals(0,
            recipientWallet.getBalance().compareTo(new BigDecimal("50.00")));

    verify(walletRepository, never()).saveAndFlush(any(Wallet.class));
    verify(transactionRepository, never())
            .saveAndFlush(any(WalletTransaction.class));
}


@Test
void transferShouldRejectTransferToOwnWallet() {
    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> walletService.transfer(
                    "sender@example.com",
                    " SENDER@example.com ",
                    new BigDecimal("10.00"),
                    "transfer-test-003"));

    assertEquals(
            "You cannot transfer money to your own wallet",
            exception.getMessage());

    verifyNoInteractions(
            userRepository, walletRepository, transactionRepository);
}


@Test
void addMoneyShouldNotProcessSameIdempotencyKeyTwice() {
    User user = createUser(1L, "test@example.com");
    Wallet wallet = createWallet(10L, user, "150.00");

    WalletTransaction existingTx = new WalletTransaction();
    existingTx.setIdempotencyKey("add-duplicate-001");
    existingTx.setToWallet(wallet);
    existingTx.setAmount(new BigDecimal("50.00"));
    existingTx.setType(TransactionType.ADD);
    existingTx.setStatus(TransactionStatus.SUCCESS);

    when(transactionRepository.findByIdempotencyKey("add-duplicate-001"))
            .thenReturn(Optional.of(existingTx));

    AddMoneyResponse response = walletService.addMoney(
            "test@example.com",
            new BigDecimal("50.00"),
            "add-duplicate-001");

    assertEquals("Request already processed", response.getMessage());
    assertEquals("SUCCESS", response.getStatus());

    verify(walletRepository, never()).saveAndFlush(any(Wallet.class));
    verify(transactionRepository, never())
            .saveAndFlush(any(WalletTransaction.class));
    verifyNoInteractions(userRepository);
}


@Test
void getTransactionHistoryShouldReturnTransactions() {
    User user = createUser(1L, "test@example.com");
    Wallet wallet = createWallet(10L, user, "150.00");

    WalletTransaction tx = new WalletTransaction();
    tx.setFromWallet(wallet);
    tx.setToWallet(wallet);
    tx.setAmount(new BigDecimal("25.00"));
    tx.setType(TransactionType.TRANSFER);
    tx.setStatus(TransactionStatus.SUCCESS);

    Pageable pageable = PageRequest.of(
            0,
            10,
            org.springframework.data.domain.Sort
                    .by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt")
    );
    Page<WalletTransaction> transactionPage =
            new PageImpl<>(List.of(tx));

    when(transactionRepository
            .findByFromWalletUserEmailOrToWalletUserEmail(
                    "test@example.com",
                    "test@example.com",
                    pageable))
            .thenReturn(transactionPage);

    Page<TransactionHistoryResponse> result =
            walletService.getTransactionHistory(
                    "test@example.com", 0, 10);

    assertEquals(1, result.getTotalElements());
    assertEquals(new BigDecimal("25.00"),
            result.getContent().get(0).getAmount());
}

@Test
void getTransactionHistoryShouldRejectNegativePage() {
    assertThrows(IllegalArgumentException.class,
            () -> walletService.getTransactionHistory(
                    "test@example.com", -1, 10));
}

@Test
void getTransactionHistoryShouldRejectZeroPageSize() {
    assertThrows(IllegalArgumentException.class,
            () -> walletService.getTransactionHistory(
                    "test@example.com", 0, 0));
}

@Test
void getTransactionHistoryShouldRejectPageSizeAbove100() {
    assertThrows(IllegalArgumentException.class,
            () -> walletService.getTransactionHistory(
                    "test@example.com", 0, 101));
}
}

