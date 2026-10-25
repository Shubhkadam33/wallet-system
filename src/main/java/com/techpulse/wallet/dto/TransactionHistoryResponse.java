package com.techpulse.wallet.dto;




import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistoryResponse {

    private Long transactionId;
    private String fromEmail;
    private String toEmail;
    private BigDecimal amount;
    private String type;
    private String status;
    private LocalDateTime createdAt;

    public TransactionHistoryResponse(
            Long transactionId,
            String fromEmail,
            String toEmail,
            BigDecimal amount,
            String type,
            String status,
            LocalDateTime createdAt) {

        this.transactionId = transactionId;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

