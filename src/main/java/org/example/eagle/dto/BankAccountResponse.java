package org.example.eagle.dto;

public record BankAccountResponse(
    String accountNumber,
    String sortCode,
    String name,
    String accountType,
    double balance,
    String currency,
    String createdTimestamp,
    String updatedTimestamp
) {}

