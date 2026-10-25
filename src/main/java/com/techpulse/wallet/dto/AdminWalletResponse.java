package com.techpulse.wallet.dto;




import java.math.BigDecimal;

public class AdminWalletResponse {

    private Long walletId;
    private String email;
    private BigDecimal balance;

    public AdminWalletResponse(
            Long walletId, String email, BigDecimal balance) {
        this.walletId = walletId;
        this.email = email;
        this.balance = balance;
    }

    public Long getWalletId() {
        return walletId;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
