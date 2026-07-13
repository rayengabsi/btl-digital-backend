package com.btl.digital.repository;

import com.btl.digital.entity.Account;
import com.btl.digital.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByOwnerId(Long ownerId);

    @Query("""
    SELECT a
    FROM Account a
    JOIN FETCH a.owner
    """)
    List<Account> findAllWithOwner();

    @Query("""
    SELECT a
    FROM Account a
    JOIN FETCH a.owner
    WHERE a.id = :id
    """)
    Optional<Account> findByIdWithOwner(Long id);

    long countByStatus(AccountStatus status);

    @Query("""
    SELECT COALESCE(SUM(a.balance),0)
    FROM Account a
    """)
    BigDecimal getTotalBankBalance();
}
