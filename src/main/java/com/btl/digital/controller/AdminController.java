package com.btl.digital.controller;

import com.btl.digital.dto.request.UpdateUserRoleRequest;
import com.btl.digital.dto.response.AdminAccountResponse;
import com.btl.digital.dto.response.AdminDashboardResponse;
import com.btl.digital.dto.response.AdminTransactionResponse;
import com.btl.digital.dto.response.AdminUserResponse;
import com.btl.digital.service.AccountService;
import com.btl.digital.service.TransactionService;
import com.btl.digital.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AdminAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AdminAccountResponse> getAccountByIdForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountByIdForAdmin(id));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<AdminUserResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<AdminUserResponse> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    @PatchMapping("/accounts/{id}/freeze")
    public ResponseEntity<AdminAccountResponse> freezeAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.freezeAccount(id));
    }

    @PatchMapping("/accounts/{id}/unfreeze")
    public ResponseEntity<AdminAccountResponse> unfreezeAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.unfreezeAccount(id));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<AdminTransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<AdminTransactionResponse> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<AdminTransactionResponse>> getTransactionsByAccount(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(userService.getDashboard());
    }
}
