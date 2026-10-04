package com.advisor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.advisor.entity.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {}

