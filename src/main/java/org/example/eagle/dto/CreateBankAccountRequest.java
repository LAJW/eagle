package org.example.eagle.dto;

public record CreateBankAccountRequest(
    String name,
    String accountType
) {}

