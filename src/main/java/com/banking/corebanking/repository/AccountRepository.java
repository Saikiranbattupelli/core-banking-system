package com.banking.corebanking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banking.corebanking.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	 Optional<Account> findByAccountNumber(String accountNumber);

	    boolean existsByAccountNumber(String accountNumber);

	    @Lock(LockModeType.PESSIMISTIC_WRITE)
	    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
	    Optional<Account> findByAccountNumberForUpdate(
	            @Param("accountNumber") String accountNumber);
}