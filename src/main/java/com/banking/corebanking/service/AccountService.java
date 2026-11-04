package com.banking.corebanking.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.banking.corebanking.entity.Account;
import com.banking.corebanking.entity.Customer;
import com.banking.corebanking.repository.AccountRepository;
import com.banking.corebanking.repository.CustomerRepository;

import com.banking.corebanking.exception.ResourceNotFoundException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    // 1. Create Account
    public Account createAccount(Long customerId, String accountType) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId));

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(accountType);
        account.setBalance(new BigDecimal("0.00"));
        account.setStatus("ACTIVE");
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    // 2. Generate Unique Account Number
    private String generateAccountNumber() {

        String accountNumber;

        do {
            accountNumber = "AC" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    // 3. Get All Accounts
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // 4. Get Account By Account Number
    public Account getAccountByNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + accountNumber));
    }
}