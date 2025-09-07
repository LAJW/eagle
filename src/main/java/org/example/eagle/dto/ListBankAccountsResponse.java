package org.example.eagle.dto;

import java.util.List;

public record ListBankAccountsResponse(
    List<BankAccountResponse> accounts
) {}
