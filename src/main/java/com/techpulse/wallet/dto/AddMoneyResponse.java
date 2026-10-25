package com.techpulse.wallet.dto;



import java.math.BigDecimal;

public class AddMoneyResponse {

    private Long transactionId;
    private BigDecimal amount;
    private BigDecimal balance;
    private String status;
    private String message;

    public AddMoneyResponse(
            Long transactionId,
            BigDecimal amount,
            BigDecimal balance,
            String status,
            String message) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.balance = balance;
        this.status = status;
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

