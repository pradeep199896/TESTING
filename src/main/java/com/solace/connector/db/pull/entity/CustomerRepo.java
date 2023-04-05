package com.solace.connector.db.pull.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<Customer, CustomerId> {



}