package com.banking.corebanking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.banking.corebanking.entity.Customer;
import com.banking.corebanking.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Constructor injection
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Create customer
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // Get customer by ID
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Customer not found with ID: " + id));
    }

    // Update customer
    public Customer updateCustomer(Long id, Customer updatedCustomer) {

        Customer existingCustomer = getCustomerById(id);

        existingCustomer.setFullName(updatedCustomer.getFullName());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setPhone(updatedCustomer.getPhone());
        existingCustomer.setAddress(updatedCustomer.getAddress());

        return customerRepository.save(existingCustomer);
    }

    // Deactivate customer
    public Customer deactivateCustomer(Long id) {

        Customer customer = getCustomerById(id);

        customer.setActive(false);

        return customerRepository.save(customer);
    }
}