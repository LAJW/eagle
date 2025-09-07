package org.example.eagle.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.eagle.dto.CreateTransactionRequest;
import org.example.eagle.dto.ListTransactionsResponse;
import org.example.eagle.dto.TransactionResponse;
import org.example.eagle.entity.Account;
import org.example.eagle.entity.Transaction;
import org.example.eagle.model.AccountNumber;
import org.example.eagle.repository.AccountRepository;
import org.example.eagle.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
public class TransactionController {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionController(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    private Account getAccountOrThrow(String accountNumber, HttpServletRequest request) {
        try {
            AccountNumber accNum = new AccountNumber(accountNumber);
            var accountOpt = accountRepository.findByAccountNumber(accNum.asInt());
            if (accountOpt.isEmpty()) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Bank account was not found");
            }
            var account = accountOpt.get();
            String userId = String.valueOf(request.getAttribute("userId"));
            if (!account.getUser().getId().toString().equals(userId)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You are not allowed to access this bank account");
            }
            return account;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid account number format");
        }
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> createTransaction(@PathVariable String accountNumber, @RequestBody CreateTransactionRequest requestBody, HttpServletRequest request) {
        Account account;
        try {
            account = getAccountOrThrow(accountNumber, request);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        if (!"GBP".equals(requestBody.currency())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body("Only GBP currency is supported");
        }
        if (requestBody.amount() <= 0 || requestBody.amount() > 10000.00) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body("Amount must be between 0.01 and 10000.00");
        }
        if (!"deposit".equals(requestBody.type()) && !"withdrawal".equals(requestBody.type())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body("Type must be deposit or withdrawal");
        }
        if ("withdrawal".equals(requestBody.type())) {
            double newBalance = account.getBalance() - requestBody.amount();
            if (newBalance <= 0) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY).body("Insufficient funds to process transaction");
            }
        }
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(requestBody.amount());
        transaction.setCurrency(requestBody.currency());
        transaction.setType(requestBody.type());
        transaction.setReference(requestBody.reference());
        transaction.setCreatedTimestamp(LocalDateTime.now());
        transaction.setUserId(account.getUser().getId().toString());
        transactionRepository.save(transaction);
        // Update account balance
        if ("deposit".equals(requestBody.type())) {
            account.setBalance(account.getBalance() + requestBody.amount());
        } else {
            account.setBalance(account.getBalance() - requestBody.amount());
        }
        accountRepository.save(account);
        return ResponseEntity.status(201).body(toResponse(transaction));
    }

    @GetMapping
    public ResponseEntity<ListTransactionsResponse> listTransactions(@PathVariable String accountNumber, HttpServletRequest request) {
        Account account;
        try {
            account = getAccountOrThrow(accountNumber, request);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        }
        List<Transaction> transactions = transactionRepository.findByAccount_AccountNumber(account.getAccountNumber());
        List<TransactionResponse> responses = transactions.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(new ListTransactionsResponse(responses));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> fetchTransactionById(@PathVariable String accountNumber, @PathVariable long transactionId, HttpServletRequest request) {
        Account account;
        try {
            account = getAccountOrThrow(accountNumber, request);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
        Transaction transaction = transactionRepository.findByIdAndAccount_AccountNumber(transactionId, account.getAccountNumber());
        if (transaction == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Transaction not found");
        }
        return ResponseEntity.ok(toResponse(transaction));
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getType(),
            transaction.getReference(),
            transaction.getUserId(),
            transaction.getCreatedTimestamp()
        );
    }
}
