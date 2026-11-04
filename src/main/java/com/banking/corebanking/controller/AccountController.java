package com.banking.corebanking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.banking.corebanking.dto.AccountRequest;
import com.banking.corebanking.dto.AccountResponse;
import com.banking.corebanking.entity.Account;
import com.banking.corebanking.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request) {

        Account account = accountService.createAccount(
                request.getCustomerId(),
                request.getAccountType()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {

        List<AccountResponse> accounts =
                accountService.getAllAccounts()
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber) {

        Account account =
                accountService.getAccountByNumber(accountNumber);

        return ResponseEntity.ok(convertToResponse(account));
    }

    private AccountResponse convertToResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getCustomer().getId()
        );
    }
}
