package com.btl.digital.service;

import com.btl.digital.dto.request.CreateAccountRequest;
import com.btl.digital.dto.response.AccountResponse;
import com.btl.digital.dto.response.AdminAccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    List<AccountResponse> getMyAccounts();

    AccountResponse getAccountById(Long id);

    List<AdminAccountResponse> getAllAccounts();

    AdminAccountResponse getAccountByIdForAdmin(Long id);

    AdminAccountResponse freezeAccount(Long id);

    AdminAccountResponse unfreezeAccount(Long id);
}
