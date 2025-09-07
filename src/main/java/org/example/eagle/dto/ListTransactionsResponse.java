package org.example.eagle.dto;

import java.util.List;

public record ListTransactionsResponse(
    List<TransactionResponse> transactions
) {}

