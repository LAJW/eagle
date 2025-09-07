package org.example.eagle.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.eagle.dto.BankAccountResponse;
import org.example.eagle.dto.CreateBankAccountRequest;
import org.example.eagle.dto.ListBankAccountsResponse;
import org.example.eagle.dto.UpdateBankAccountRequest;
import org.example.eagle.entity.Account;
import org.example.eagle.model.AccountNumber;
import org.example.eagle.repository.AccountRepository;
import org.example.eagle.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {
    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    public AccountController(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(@RequestBody CreateBankAccountRequest request, HttpServletRequest context) {
        var currentUserId = (long) context.getAttribute("userId");
        var user = userRepository.findById(currentUserId);
        if (user.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Account account = new Account();
        account.setSortCode("10-10-10");
        account.setName(request.name());
        account.setAccountType(request.accountType());
        account.setBalance(0.0);
        account.setCurrency("GBP");
        account.setCreatedTimestamp(LocalDateTime.now());
        account.setUpdatedTimestamp(LocalDateTime.now());
        account.setUser(user.get());
        accountRepository.save(account);
        BankAccountResponse response = toResponse(account);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<ListBankAccountsResponse> listAccounts() {
        List<Account> accounts = accountRepository.findAll();
        List<BankAccountResponse> responses = accounts.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(new ListBankAccountsResponse(responses));
    }

    private Account getAccountOrThrow(String accountNumber, HttpServletRequest request) {
        try {
            AccountNumber accNum = new AccountNumber(accountNumber);
            var accountOpt = accountRepository.findByAccountNumber(accNum.asInt());
            if (accountOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank account was not found");
            }
            var account = accountOpt.get();
            var userId = (long) request.getAttribute("userId");
            if (account.getUser().getId() != userId) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this bank account");
            }
            return account;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account number format");
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> fetchAccountByAccountNumber(@PathVariable String accountNumber, HttpServletRequest request) {
        Account account = getAccountOrThrow(accountNumber, request);
        return ResponseEntity.ok(toResponse(account));
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<?> updateAccountByAccountNumber(@PathVariable String accountNumber, @RequestBody UpdateBankAccountRequest requestBody, HttpServletRequest request) {
        Account account = getAccountOrThrow(accountNumber, request);
        if (requestBody.name() != null) account.setName(requestBody.name());
        if (requestBody.accountType() != null) account.setAccountType(requestBody.accountType());
        account.setUpdatedTimestamp(LocalDateTime.now());
        accountRepository.save(account);
        return ResponseEntity.ok(toResponse(account));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<?> deleteAccountByAccountNumber(@PathVariable String accountNumber) {
        AccountNumber accNum;
        try {
            accNum = new AccountNumber(accountNumber);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid account number format");
        }
        var account = accountRepository.findByAccountNumber(accNum.asInt());
        if (account.isEmpty()) {
            return ResponseEntity.status(404).body("Bank account was not found");
        }
        accountRepository.delete(account.get());
        return ResponseEntity.noContent().build();
    }

    private BankAccountResponse toResponse(Account account) {
        return new BankAccountResponse(
                new AccountNumber(account.getAccountNumber()).toString(),
                account.getSortCode(),
                account.getName(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedTimestamp().toString(),
                account.getUpdatedTimestamp().toString()
        );
    }
}
