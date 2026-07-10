package com.btl.digital.service;

import com.btl.digital.dto.request.DepositRequest;
import com.btl.digital.dto.request.WithdrawRequest;
import com.btl.digital.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse withdraw(WithdrawRequest request);

    List<TransactionResponse> getAccountTransactions(Long accountId);
}
