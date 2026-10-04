package com.advisor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.advisor.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {}

