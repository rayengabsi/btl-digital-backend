package com.btl.digital.mock.service;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.dto.transfer.ConfirmIntraTransferRequest;
import com.btl.digital.mock.dto.transfer.IntraTransferInitiationResponse;
import com.btl.digital.mock.dto.transfer.IntraTransferRequest;
import com.btl.digital.mock.dto.transfer.IntraTransferResponse;
import com.btl.digital.mock.dto.transfer.PageResponse;
import com.btl.digital.mock.dto.transfer.TransferDemandResponse;
import com.btl.digital.mock.model.MockAccount;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.model.MockOtp;
import com.btl.digital.mock.model.MockTransaction;
import com.btl.digital.mock.model.MockTransferDemand;
import com.btl.digital.mock.security.MockAuthContext;
import com.btl.digital.mock.store.MockDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MockTransferService {

    private static final Logger log = LoggerFactory.getLogger(MockTransferService.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final MockDataStore store;
    private final MockAuthContext authContext;
    private final MockOtpProvider otpProvider;

    public MockTransferService(MockDataStore store, MockAuthContext authContext, MockOtpProvider otpProvider) {
        this.store = store;
        this.authContext = authContext;
        this.otpProvider = otpProvider;
    }

    public IntraTransferInitiationResponse initiate(IntraTransferRequest request) {
        MockClientUser client = authContext.requireClient();
        requirePermission(client, "MAKE_INTRA_TRANSFER");

        MockAccount debit = store.accountByAccountId(request.getDebitAccountId())
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_FOUND", "Debit account not found."));
        if (!debit.getOwnerClientUserId().equals(client.getId())) {
            throw new MockApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Debit account is not owned by the authenticated client.");
        }
        MockAccount credit = store.accountByAccountId(request.getCreditAccountId())
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_FOUND", "Credit account not found."));
        if (debit.getAccountId().equals(credit.getAccountId())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Debit and credit accounts cannot be the same.");
        }
        if (debit.getWorkingBalance().compareTo(request.getDebitAmount()) < 0) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS", "Insufficient funds for the transfer.");
        }

        MockOtp otp = otpProvider.generate(5);

        MockTransferDemand demand = MockTransferDemand.builder()
                .id(store.nextTransferDemandId())
                .clientUserId(client.getId())
                .amount(request.getDebitAmount())
                .transactionType(request.getTransactionType())
                .debitAccountId(request.getDebitAccountId())
                .debitCurrency(request.getDebitCurrency())
                .creditAccountId(request.getCreditAccountId())
                .paymentDetails(request.getPaymentDetails().stream().map(p -> p.getPaymentDetail()).toList())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .otp(otp.getCode())
                .otpExpiresAt(otp.getExpiresAt())
                .otpAttempts(0)
                .build();
        store.addTransferDemand(demand);

        log.info("[MOCK TRANSFER OTP] demandId={} to client {} code={} expiresAt={}",
                demand.getId(), client.getEmail(), otp.getCode(), otp.getExpiresAt().format(ISO));

        return IntraTransferInitiationResponse.builder()
                .transferDemandId(demand.getId())
                .status("PENDING")
                .otpExpiresAt(otp.getExpiresAt().format(ISO))
                .build();
    }

    public IntraTransferResponse confirm(ConfirmIntraTransferRequest request) {
        MockClientUser client = authContext.requireClient();
        requirePermission(client, "MAKE_INTRA_TRANSFER");

        MockTransferDemand demand = store.transferDemandById(request.getTransferDemandId())
                .filter(d -> d.getClientUserId().equals(client.getId()))
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "TRANSFER_DEMAND_NOT_FOUND",
                        "Demand does not exist or belongs to another user."));

        if (!"PENDING".equals(demand.getStatus())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "TRANSFER_ALREADY_PROCESSED",
                    "Transfer was already confirmed or rejected.");
        }
        if (demand.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            demand.setStatus("FAILED");
            demand.setCompletedAt(LocalDateTime.now());
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "OTP has expired.");
        }
        if (demand.getOtpAttempts() >= 3) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_MAX_ATTEMPTS_EXCEEDED", "Too many OTP attempts.");
        }
        if (!demand.getOtp().equals(request.getOtp())) {
            demand.setOtpAttempts(demand.getOtpAttempts() + 1);
            throw new MockApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "OTP is incorrect.");
        }

        MockAccount debit = store.accountByAccountId(demand.getDebitAccountId()).orElseThrow();
        MockAccount credit = store.accountByAccountId(demand.getCreditAccountId()).orElseThrow();
        debit.setWorkingBalance(debit.getWorkingBalance().subtract(demand.getAmount()));
        credit.setWorkingBalance(credit.getWorkingBalance().add(demand.getAmount()));

        demand.setStatus("SUCCESSFUL");
        demand.setCompletedAt(LocalDateTime.now());
        demand.setTransactionId("FT" + System.currentTimeMillis() % 1_000_000_000);

        List<String> narratives = List.of(demand.getTransactionType() + " to " + credit.getAccountId());
        store.addTransaction(demand.getDebitAccountId(), newTransaction(demand.getDebitAccountId(), demand.getTransactionId(),
                "Transfer Out", LocalDate.now(), demand.getAmount(), null, narratives));
        store.addTransaction(demand.getCreditAccountId(), newTransaction(demand.getCreditAccountId(), demand.getTransactionId(),
                "Transfer In", LocalDate.now(), null, demand.getAmount(), narratives));

        return IntraTransferResponse.builder()
                .transactionId(demand.getTransactionId())
                .status("SUCCESS")
                .transactionStatus("BOOKED")
                .transactionType(demand.getTransactionType())
                .debitAccountId(demand.getDebitAccountId())
                .debitAccountIban(debit.getIban())
                .debitCurrency(demand.getDebitCurrency())
                .debitAmount(demand.getAmount())
                .debitValueDate(formattedValueDate())
                .creditAccountId(demand.getCreditAccountId())
                .creditAccountIban(credit.getIban())
                .creditCurrencyId(credit.getCurrencyId())
                .creditValueDate(formattedValueDate())
                .commissionCode("STANDARD")
                .profitCentreCustomer(client.getT24CustomerId())
                .paymentDetails(List.of(IntraTransferResponse.PaymentDetailResponse.builder()
                        .paymentDetail(String.join(", ", demand.getPaymentDetails())).build()))
                .overrideDetails(List.of())
                .build();
    }

    public PageResponse<TransferDemandResponse> getHistory(Integer page, Integer size, LocalDate from, LocalDate to) {
        MockClientUser client = authContext.requireClient();

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null ? 20 : Math.min(size, 100);

        List<MockTransferDemand> all = new ArrayList<>(store.transferDemandsForClient(client.getId()));
        all.sort(Comparator.comparing(MockTransferDemand::getCreatedAt).reversed()
                .thenComparing(MockTransferDemand::getId, Comparator.reverseOrder()));

        if (from != null) {
            all.removeIf(d -> d.getCreatedAt().toLocalDate().isBefore(from));
        }
        if (to != null) {
            all.removeIf(d -> d.getCreatedAt().toLocalDate().isAfter(to));
        }

        int fromIdx = Math.min(safePage * safeSize, all.size());
        int toIdx = Math.min(fromIdx + safeSize, all.size());
        List<TransferDemandResponse> content = all.subList(fromIdx, toIdx).stream()
                .map(this::toHistoryDto)
                .toList();

        int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) all.size() / safeSize);

        return PageResponse.<TransferDemandResponse>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(all.size())
                .totalPages(totalPages)
                .first(safePage == 0)
                .last(safePage >= totalPages - 1)
                .build();
    }

    public TransferDemandResponse getById(Long id) {
        MockClientUser client = authContext.requireClient();
        return store.transferDemandById(id)
                .filter(d -> d.getClientUserId().equals(client.getId()))
                .map(this::toHistoryDto)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "TRANSFER_DEMAND_NOT_FOUND",
                        "Demand does not exist or belongs to another user."));
    }

    private TransferDemandResponse toHistoryDto(MockTransferDemand d) {
        return TransferDemandResponse.builder()
                .id(d.getId())
                .clientUserId(d.getClientUserId())
                .amount(d.getAmount())
                .transactionType(d.getTransactionType())
                .debitAccountId(d.getDebitAccountId())
                .debitCurrency(d.getDebitCurrency())
                .creditAccountId(d.getCreditAccountId())
                .paymentDetails(d.getPaymentDetails())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt().format(ISO))
                .completedAt(d.getCompletedAt() == null ? null : d.getCompletedAt().format(ISO))
                .build();
    }

    private void requirePermission(MockClientUser client, String permission) {
        if (!store.permissionsGrantedToClient(client.getId()).contains(permission)) {
            throw new MockApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "User does not have the required permission: " + permission);
        }
    }

    private String formattedValueDate() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private MockTransaction newTransaction(String accountId, String ref, String code,
                                           LocalDate date, BigDecimal debit, BigDecimal credit,
                                           List<String> narratives) {
        return MockTransaction.builder()
                .accountId(accountId)
                .transactionReference(ref)
                .transactionCode(code)
                .bookingDate(date)
                .valueDate(date)
                .debitAmount(debit)
                .creditAmount(credit)
                .narratives(narratives)
                .build();
    }
}