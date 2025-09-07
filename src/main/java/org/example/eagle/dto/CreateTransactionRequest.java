package org.example.eagle.dto;

public record CreateTransactionRequest(
    double amount,
    String currency,
    String type,
    String reference
) {}

