
package com.banking.corebanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banking.corebanking.entity.BankTransaction;

@Repository
public interface TransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Optional<BankTransaction> findByReferenceNumber(String referenceNumber);

}
