package com.techpulse.wallet.controller;



import com.techpulse.wallet.dto.AdminWalletResponse;
import com.techpulse.wallet.dto.TransactionHistoryResponse;
import com.techpulse.wallet.entity.User;
import com.techpulse.wallet.entity.Wallet;
import com.techpulse.wallet.entity.WalletTransaction;
import com.techpulse.wallet.enums.Role;
import com.techpulse.wallet.enums.TransactionStatus;
import com.techpulse.wallet.enums.TransactionType;
import com.techpulse.wallet.repository.WalletRepository;
import com.techpulse.wallet.repository.WalletTransactionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private AdminController adminController;

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
    void getAllWalletsShouldReturnWalletList() {
        User user = createUser(1L, "test@example.com");
        Wallet wallet = createWallet(10L, user, "150.00");

        when(walletRepository.findAll()).thenReturn(List.of(wallet));

        ResponseEntity<List<AdminWalletResponse>> response =
                adminController.getAllWallets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("test@example.com",
                response.getBody().get(0).getEmail());
        assertEquals(0,
                response.getBody().get(0).getBalance()
                        .compareTo(new BigDecimal("150.00")));

        verify(walletRepository).findAll();
    }

    @Test
    void getAllTransactionsShouldReturnTransactionList() {
        User sender = createUser(1L, "sender@example.com");
        User recipient = createUser(2L, "recipient@example.com");

        Wallet senderWallet = createWallet(10L, sender, "100.00");
        Wallet recipientWallet = createWallet(20L, recipient, "50.00");

        WalletTransaction tx = new WalletTransaction();
        tx.setFromWallet(senderWallet);
        tx.setToWallet(recipientWallet);
        tx.setAmount(new BigDecimal("25.00"));
        tx.setType(TransactionType.TRANSFER);
        tx.setStatus(TransactionStatus.SUCCESS);
        

        when(transactionRepository.findAll()).thenReturn(List.of(tx));

        ResponseEntity<List<TransactionHistoryResponse>> response =
                adminController.getAllTransactions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("sender@example.com",
                response.getBody().get(0).getFromEmail());
        assertEquals("recipient@example.com",
                response.getBody().get(0).getToEmail());
        assertEquals("TRANSFER", response.getBody().get(0).getType());
        assertEquals("SUCCESS", response.getBody().get(0).getStatus());

        verify(transactionRepository).findAll();
    }
}

