package com.btl.digital.service.impl;

import com.btl.digital.dto.request.UpdateUserRoleRequest;
import com.btl.digital.dto.response.AdminUserResponse;
import com.btl.digital.dto.response.UserProfileResponse;
import com.btl.digital.entity.User;
import com.btl.digital.enums.AccountStatus;
import com.btl.digital.exception.ResourceNotFoundException;
import com.btl.digital.repository.UserRepository;
import com.btl.digital.service.UserService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
}
