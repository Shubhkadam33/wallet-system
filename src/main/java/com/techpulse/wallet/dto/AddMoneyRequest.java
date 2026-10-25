package com.techpulse.wallet.dto;



import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class AddMoneyRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(
        value = "0.01",
        message = "Amount must be greater than zero"
    )
    private BigDecimal amount;

    public AddMoneyRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

