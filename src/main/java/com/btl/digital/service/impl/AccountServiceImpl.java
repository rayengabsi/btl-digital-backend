package com.btl.digital.service.impl;

import com.btl.digital.dto.request.CreateAccountRequest;
import com.btl.digital.dto.response.AccountResponse;
import com.btl.digital.entity.Account;
import com.btl.digital.entity.User;
import com.btl.digital.enums.AccountStatus;
import com.btl.digital.enums.Currency;
import com.btl.digital.enums.Role;
import com.btl.digital.repository.AccountRepository;
import com.btl.digital.repository.UserRepository;
import com.btl.digital.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String iban = "TN" + "00" + accountNumber;

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .iban(iban)
                .balance(BigDecimal.ZERO)
                .currency(Currency.TND)
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .owner(owner)
                .build();

        accountRepository.save(account);

        return mapToResponse(account);
    }

    @Override
    public List<AccountResponse> getMyAccounts() {
        User currentUser = getAuthenticatedUser();

        List<Account> accounts = accountRepository.findByOwnerId(currentUser.getId());

        return accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        User currentUser = getAuthenticatedUser();

        if (currentUser.getRole() != Role.ADMIN && !account.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToResponse(account);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .iban(account.getIban())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .ownerId(account.getOwner().getId())
                .build();
    }
}
