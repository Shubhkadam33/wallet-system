package com.techpulse.wallet.dto;



import java.math.BigDecimal;

public class TransferResponse {

    private Long transactionId;
    private String fromEmail;
    private String toEmail;
    private BigDecimal amount;
    private BigDecimal senderBalance;
    private String status;
    private String message;

    public TransferResponse(Long transactionId, String fromEmail,
            String toEmail, BigDecimal amount, BigDecimal senderBalance,
            String status, String message) {
        this.transactionId = transactionId;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.amount = amount;
        this.senderBalance = senderBalance;
        this.status = status;
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getToEmail() {
        return toEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getSenderBalance() {
        return senderBalance;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

