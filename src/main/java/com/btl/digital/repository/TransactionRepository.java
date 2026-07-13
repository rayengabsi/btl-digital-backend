package com.btl.digital.repository;

import com.btl.digital.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);

    long count();

    @Query("""
    SELECT COALESCE(SUM(t.amount),0)
    FROM Transaction t
    WHERE t.transactionType='DEPOSIT'
    """)
    BigDecimal getTotalDeposits();

    @Query("""
    SELECT COALESCE(SUM(t.amount),0)
    FROM Transaction t
    WHERE t.transactionType='WITHDRAWAL'
    """)
    BigDecimal getTotalWithdrawals();

    @Query("""
    SELECT COALESCE(SUM(t.amount),0)
    FROM Transaction t
    WHERE t.transactionType IN ('TRANSFER_IN','TRANSFER_OUT')
    """)
    BigDecimal getTotalTransfers();
}
