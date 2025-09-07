package org.example.eagle.dto;

import java.time.LocalDateTime;

public record TransactionResponse(
    long id,
    double amount,
    String currency,
    String type,
    String reference,
    String userId,
    LocalDateTime createdTimestamp
) {}

