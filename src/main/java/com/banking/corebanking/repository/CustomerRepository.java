package com.banking.corebanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banking.corebanking.entity.Customer;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

}
