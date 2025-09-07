package org.example.eagle.dto;

public record UpdateBankAccountRequest(
    String name,
    String accountType
) {}

