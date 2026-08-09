package com.btl.digital.mock.service;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.dto.account.AccountTransactionsResponse;
import com.btl.digital.mock.dto.account.CustomerAccount;
import com.btl.digital.mock.dto.dev.DevAccountDepositRequest;
import com.btl.digital.mock.dto.dev.DevAccountDepositResponse;
import com.btl.digital.mock.model.MockAccount;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.model.MockTransaction;
import com.btl.digital.mock.security.MockAuthContext;
import com.btl.digital.mock.store.MockDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MockAccountService {

    private final MockDataStore store;
    private final MockAuthContext authContext;

    public MockAccountService(MockDataStore store, MockAuthContext authContext) {
        this.store = store;
        this.authContext = authContext;
    }

    public List<CustomerAccount> getMyAccounts() {
        MockClientUser client = authContext.requireClient();
        return store.accountsForClient(client.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Development-only: credits fake money to the authenticated client's primary
     * mock account and records a matching statement transaction. The balances
     * updated here are the same ones read by GET /api/v1/accounts and
     * POST /api/v1/transfers/intra.
     */
    public DevAccountDepositResponse depositToPrimaryAccount(DevAccountDepositRequest request) {
        MockClientUser client = authContext.requireClient();

        MockAccount primary = store.accountsForClient(client.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND",
                        "No mock account found for the authenticated client."));

        primary.setAvailableBalance(primary.getAvailableBalance().add(request.getAmount()));
        primary.setWorkingBalance(primary.getWorkingBalance().add(request.getAmount()));

        String reference = "DEP" + System.currentTimeMillis() % 1_000_000_000;
        store.addTransaction(primary.getAccountId(), MockTransaction.builder()
                .accountId(primary.getAccountId())
                .transactionReference(reference)
                .transactionCode("Deposit")
                .bookingDate(LocalDate.now())
                .valueDate(LocalDate.now())
                .closingBalance(primary.getAvailableBalance())
                .debitAmount(null)
                .creditAmount(request.getAmount())
                .narratives(List.of("Dev deposit"))
                .build());

        return DevAccountDepositResponse.builder()
                .accountId(primary.getAccountId())
                .iban(primary.getIban())
                .currencyId(primary.getCurrencyId())
                .amount(request.getAmount())
                .availableBalance(primary.getAvailableBalance())
                .workingBalance(primary.getWorkingBalance())
                .build();
    }

    public AccountTransactionsResponse getTransactions(String iban, LocalDate fromDate, LocalDate toDate,
                                                       Integer pageSize, Integer pageStart, String pageToken) {
        MockClientUser client = authContext.requireClient();

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "INVALID_TRANSACTION_DATE_RANGE",
                    "fromDate must be before or equal to toDate.");
        }
        if (pageSize != null && (pageSize < 1 || pageSize > 100)) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                    "pageSize must be between 1 and 100.");
        }
        if (pageStart != null && pageStart < 0) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                    "pageStart must be zero or greater.");
        }

        if (!store.clientOwnsAccountIban(client.getId(), iban)) {
            throw new MockApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Requested IBAN is not owned by the authenticated customer.");
        }

        MockAccount account = store.accountByIban(iban)
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_FOUND", "Account not found."));

        List<MockTransaction> all = new ArrayList<>(store.transactionsForAccount(account.getAccountId()));
        all.sort(Comparator.comparing(MockTransaction::getBookingDate).reversed());
        all.removeIf(t -> (fromDate != null && t.getBookingDate() != null && t.getBookingDate().isBefore(fromDate))
                || (toDate != null && t.getBookingDate() != null && t.getBookingDate().isAfter(toDate)));

        int size = pageSize == null ? 20 : pageSize;
        int start = pageStart == null ? 0 : pageStart;
        int from = Math.min(start, all.size());
        int to = Math.min(from + size, all.size());
        List<MockTransaction> page = all.subList(from, to);

        AccountTransactionsResponse.Pagination pagination = AccountTransactionsResponse.Pagination.builder()
                .pageStart(start)
                .pageSize(size)
                .totalSize(all.size())
                .pageToken(all.size() > to ? buildToken(account.getAccountId(), from, size) : null)
                .build();

        return AccountTransactionsResponse.builder()
                .summary(null)
                .pagination(pagination)
                .transactions(page.stream().map(t -> toTxnDto(t, account)).toList())
                .build();
    }

    private String buildToken(String accountId, int from, int size) {
        return "mock-" + accountId + "-" + from + "-" + size;
    }

    private CustomerAccount toDto(MockAccount a) {
        return CustomerAccount.builder()
                .accountId(a.getAccountId())
                .arrangementId(a.getArrangementId())
                .productName(a.getProductName())
                .iban(a.getIban())
                .currencyId(a.getCurrencyId())
                .availableBalance(a.getAvailableBalance())
                .workingBalance(a.getWorkingBalance())
                .build();
    }

    private AccountTransactionsResponse.Transaction toTxnDto(MockTransaction t, MockAccount account) {
        return AccountTransactionsResponse.Transaction.builder()
                .accountId(account.getAccountId())
                .transactionReference(t.getTransactionReference())
                .transactionCode(t.getTransactionCode())
                .bookingDate(formatT24Date(t.getBookingDate()))
                .valueDate(t.getValueDate() == null ? null : formatT24Date(t.getValueDate()))
                .closingBalance(t.getClosingBalance())
                .debitAmount(t.getDebitAmount())
                .creditAmount(t.getCreditAmount())
                .narratives(t.getNarratives())
                .build();
    }

    private String formatT24Date(LocalDate date) {
        return date == null ? null : date.format(DateTimeFormatter.ofPattern("dd MMM yyyy").withLocale(java.util.Locale.ENGLISH)).toUpperCase();
    }
}