package com.btl.digital.repository;

import com.btl.digital.entity.User;
import com.btl.digital.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByStatus(AccountStatus status);
}
