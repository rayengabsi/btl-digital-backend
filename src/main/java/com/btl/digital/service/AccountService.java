package com.btl.digital.service;

import com.btl.digital.dto.request.CreateAccountRequest;
import com.btl.digital.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    List<AccountResponse> getMyAccounts();

    AccountResponse getAccountById(Long id);
}
