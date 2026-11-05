package com.banking.corebanking.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.corebanking.dto.TransactionRequest;
import com.banking.corebanking.dto.TransactionResponse;
import com.banking.corebanking.entity.Account;
import com.banking.corebanking.entity.BankTransaction;
import com.banking.corebanking.exception.InvalidTransactionException;
import com.banking.corebanking.exception.ResourceNotFoundException;
import com.banking.corebanking.repository.AccountRepository;
import com.banking.corebanking.repository.TransactionRepository;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {

        // 1. Lock and retrieve the account
        Account account = accountRepository
                .findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found"));

        // 2. Check account status
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new InvalidTransactionException(
                    "Account is not active");
        }

        // 3. Validate amount
        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransactionException(
                    "Deposit amount must be greater than zero");
        }

        // 4. Increase account balance
        BigDecimal newBalance = account.getBalance()
                .add(request.getAmount());

        account.setBalance(newBalance);

        accountRepository.save(account);

        // 5. Create transaction record
        BankTransaction transaction = new BankTransaction();

        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(request.getDescription());
        transaction.setToAccount(account);

        BankTransaction savedTransaction =
                transactionRepository.save(transaction);

        // 6. Return transaction response
        return new TransactionResponse(
                savedTransaction.getReferenceNumber(),
                savedTransaction.getTransactionType(),
                savedTransaction.getAmount(),
                savedTransaction.getStatus(),
                savedTransaction.getDescription(),
                savedTransaction.getCreatedAt()
        );
    }

    private String generateReferenceNumber() {

        return "TXN-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
    
}