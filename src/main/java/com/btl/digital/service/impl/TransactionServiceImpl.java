package com.btl.digital.service.impl;

import com.btl.digital.dto.request.DepositRequest;
import com.btl.digital.dto.request.TransferRequest;
import com.btl.digital.dto.request.WithdrawRequest;
import com.btl.digital.dto.response.TransactionResponse;
import com.btl.digital.dto.response.TransferResponse;
import com.btl.digital.entity.Account;
import com.btl.digital.entity.Transaction;
import com.btl.digital.enums.TransactionType;
import com.btl.digital.exception.InsufficientBalanceException;
import com.btl.digital.exception.ResourceNotFoundException;
import com.btl.digital.repository.AccountRepository;
import com.btl.digital.repository.TransactionRepository;
import com.btl.digital.service.TransactionService;
import com.btl.digital.entity.User;
import com.btl.digital.enums.Role;
import com.btl.digital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

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

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Find sender account
        Account sender = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        if (!sender.getOwner().getId().equals(authUser.getId()) && authUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied");
        }

        // 2. Find receiver account
        Account receiver = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        // 3. Check sender and receiver are not the same
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // 4. Check sufficient balance
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 5. Subtract amount from sender
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));

        // 6. Add amount to receiver
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        // 7. Save both accounts
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 8. Create TRANSFER_OUT transaction linked to sender
        Transaction transferOut = Transaction.builder()
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER_OUT)
                .description(request.getDescription())
                .account(sender)
                .build();

        // 9. Create TRANSFER_IN transaction linked to receiver
        Transaction transferIn = Transaction.builder()
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER_IN)
                .description(request.getDescription())
                .account(receiver)
                .build();

        // 10. Save both transactions
        transactionRepository.save(transferOut);
        transactionRepository.save(transferIn);

        // 11. Return TransferResponse
        return TransferResponse.builder()
                .fromAccountId(sender.getId())
                .toAccountId(receiver.getId())
                .amount(request.getAmount())
                .message("Transfer completed successfully")
                .transactionDate(transferOut.getTransactionDate())
                .build();
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
