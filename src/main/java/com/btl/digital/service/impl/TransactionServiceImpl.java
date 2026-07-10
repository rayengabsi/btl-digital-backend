package com.btl.digital.service.impl;

import com.btl.digital.dto.request.DepositRequest;
import com.btl.digital.dto.request.WithdrawRequest;
import com.btl.digital.dto.response.TransactionResponse;
import com.btl.digital.entity.Account;
import com.btl.digital.entity.Transaction;
import com.btl.digital.enums.TransactionType;
import com.btl.digital.exception.InsufficientBalanceException;
import com.btl.digital.exception.ResourceNotFoundException;
import com.btl.digital.repository.AccountRepository;
import com.btl.digital.repository.TransactionRepository;
import com.btl.digital.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .description(request.getDescription())
                .account(account)
                .build();

        transactionRepository.save(transaction);

        return mapToResponse(transaction);
    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .transactionType(TransactionType.WITHDRAWAL)
                .description(request.getDescription())
                .account(account)
                .build();

        transactionRepository.save(transaction);

        return mapToResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getAccountTransactions(Long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId);

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .accountId(transaction.getAccount().getId())
                .build();
    }
}
