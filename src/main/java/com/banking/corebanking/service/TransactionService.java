package com.banking.corebanking.service;

import com.banking.corebanking.dto.TransferRequest;

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
    
    @Transactional
    public TransactionResponse withdraw(TransactionRequest request) {

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

        // 3. Validate withdrawal amount
        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransactionException(
                    "Withdrawal amount must be greater than zero");
        }

        // 4. Check sufficient balance
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InvalidTransactionException(
                    "Insufficient account balance");
        }

        // 5. Deduct amount from account balance
        BigDecimal newBalance = account.getBalance()
                .subtract(request.getAmount());

        account.setBalance(newBalance);

        accountRepository.save(account);

        // 6. Create transaction record
        BankTransaction transaction = new BankTransaction();

        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(request.getDescription());
        transaction.setFromAccount(account);

        BankTransaction savedTransaction =
                transactionRepository.save(transaction);

        // 7. Return response
        return new TransactionResponse(
                savedTransaction.getReferenceNumber(),
                savedTransaction.getTransactionType(),
                savedTransaction.getAmount(),
                savedTransaction.getStatus(),
                savedTransaction.getDescription(),
                savedTransaction.getCreatedAt()
        );
    }
    

@Transactional
public TransactionResponse transfer(TransferRequest request) {

    String fromNumber = request.getFromAccountNumber();
    String toNumber = request.getToAccountNumber();

    // 1. Prevent transferring to the same account
    if (fromNumber.equals(toNumber)) {
        throw new InvalidTransactionException(
                "Sender and receiver accounts must be different");
    }

    // 2. Validate transfer amount
    if (request.getAmount() == null
            || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new InvalidTransactionException(
                "Transfer amount must be greater than zero");
    }

    // 3. Lock accounts in a consistent order to reduce deadlocks
    Account firstAccount;
    Account secondAccount;

    if (fromNumber.compareTo(toNumber) < 0) {

        firstAccount = accountRepository
                .findByAccountNumberForUpdate(fromNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sender account not found"));

        secondAccount = accountRepository
                .findByAccountNumberForUpdate(toNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Receiver account not found"));

    } else {

        firstAccount = accountRepository
                .findByAccountNumberForUpdate(toNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Receiver account not found"));

        secondAccount = accountRepository
                .findByAccountNumberForUpdate(fromNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sender account not found"));
    }

    // 4. Identify sender and receiver
    Account sender = fromNumber.equals(firstAccount.getAccountNumber())
            ? firstAccount : secondAccount;

    Account receiver = toNumber.equals(firstAccount.getAccountNumber())
            ? firstAccount : secondAccount;

    // 5. Check account status
    if (!"ACTIVE".equalsIgnoreCase(sender.getStatus())) {
        throw new InvalidTransactionException(
                "Sender account is not active");
    }

    if (!"ACTIVE".equalsIgnoreCase(receiver.getStatus())) {
        throw new InvalidTransactionException(
                "Receiver account is not active");
    }

    // 6. Check sufficient balance
    if (sender.getBalance().compareTo(request.getAmount()) < 0) {
        throw new InvalidTransactionException(
                "Insufficient balance in sender account");
    }

    // 7. Deduct amount from sender
    sender.setBalance(
            sender.getBalance().subtract(request.getAmount()));

    // 8. Add amount to receiver
    receiver.setBalance(
            receiver.getBalance().add(request.getAmount()));

    // 9. Save both account balances
    accountRepository.save(sender);
    accountRepository.save(receiver);

    // 10. Create transfer transaction record
    BankTransaction transaction = new BankTransaction();

    transaction.setReferenceNumber(generateReferenceNumber());
    transaction.setTransactionType("TRANSFER");
    transaction.setAmount(request.getAmount());
    transaction.setStatus("SUCCESS");
    transaction.setDescription(request.getDescription());
    transaction.setFromAccount(sender);
    transaction.setToAccount(receiver);

    BankTransaction savedTransaction =
            transactionRepository.save(transaction);

    // 11. Return response
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