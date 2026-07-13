package com.btl.digital.service.impl;

import com.btl.digital.dto.request.ChangePasswordRequest;
import com.btl.digital.dto.request.UpdateProfileRequest;
import com.btl.digital.dto.request.UpdateUserRoleRequest;
import com.btl.digital.dto.response.AdminDashboardResponse;
import com.btl.digital.dto.response.AdminUserResponse;
import com.btl.digital.dto.response.UserProfileResponse;
import com.btl.digital.entity.User;
import com.btl.digital.enums.AccountStatus;
import com.btl.digital.exception.AccessDeniedException;
import com.btl.digital.exception.ResourceNotFoundException;
import com.btl.digital.repository.AccountRepository;
import com.btl.digital.repository.TransactionRepository;
import com.btl.digital.repository.UserRepository;
import com.btl.digital.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    @Override
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        User updatedUser = userRepository.save(user);

        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .email(updatedUser.getEmail())
                .phoneNumber(updatedUser.getPhoneNumber())
                .role(updatedUser.getRole())
                .status(updatedUser.getStatus())
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AccessDeniedException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Override
    public AdminUserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToAdminResponse(user);
    }

    private AdminUserResponse mapToAdminResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public AdminUserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        return mapToAdminResponse(user);
    }

    @Override
    public AdminUserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
        return mapToAdminResponse(user);
    }

    @Override
    public AdminUserResponse updateUserRole(Long id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(request.getRole());
        userRepository.save(user);
        return mapToAdminResponse(user);
    }

    @Override
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(AccountStatus.ACTIVE))
                .inactiveUsers(userRepository.countByStatus(AccountStatus.INACTIVE))
                .totalAccounts(accountRepository.count())
                .activeAccounts(accountRepository.countByStatus(AccountStatus.ACTIVE))
                .frozenAccounts(accountRepository.countByStatus(AccountStatus.FROZEN))
                .totalTransactions(transactionRepository.count())
                .totalBankBalance(accountRepository.getTotalBankBalance())
                .totalDeposits(transactionRepository.getTotalDeposits())
                .totalWithdrawals(transactionRepository.getTotalWithdrawals())
                .totalTransfers(transactionRepository.getTotalTransfers())
                .build();
    }
}
