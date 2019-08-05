package com.saumesh.payment.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.UUID;

public class TransferDTO {
    @JsonProperty
    private long sourceAccountNumber;
    @JsonProperty
    private long targetAccountNumber;;
    @JsonProperty
    private double amount;
    @JsonProperty
    private UUID transactionId;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    public long getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(long sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public long getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public void setTargetAccountNumber(long targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
