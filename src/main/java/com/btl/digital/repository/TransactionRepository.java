package com.btl.digital.repository;

import com.btl.digital.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
}
