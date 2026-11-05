
package com.banking.corebanking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banking.corebanking.entity.BankTransaction;


@Repository
public interface TransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Optional<BankTransaction> findByReferenceNumber(String referenceNumber);

    List<BankTransaction> findAllByOrderByCreatedAtDesc();

    List<BankTransaction>
    findByFromAccount_AccountNumberOrToAccount_AccountNumberOrderByCreatedAtDesc(
            String fromAccountNumber,
            String toAccountNumber
    );
    
}
