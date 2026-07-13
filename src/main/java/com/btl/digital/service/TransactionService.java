package com.btl.digital.service;

import com.btl.digital.dto.request.DepositRequest;
import com.btl.digital.dto.request.TransferRequest;
import com.btl.digital.dto.request.WithdrawRequest;
import com.btl.digital.dto.response.AdminTransactionResponse;
import com.btl.digital.dto.response.TransactionResponse;
import com.btl.digital.dto.response.TransferResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse withdraw(WithdrawRequest request);

    List<TransactionResponse> getAccountTransactions(Long accountId);

    TransferResponse transfer(TransferRequest request);

    List<AdminTransactionResponse> getAllTransactions();

    AdminTransactionResponse getTransactionById(Long id);

    List<AdminTransactionResponse> getTransactionsByAccount(Long accountId);
}
