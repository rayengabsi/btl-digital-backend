package com.btl.digital.service;

import com.btl.digital.dto.request.UpdateUserRoleRequest;
import com.btl.digital.dto.response.AdminUserResponse;
import com.btl.digital.dto.response.UserProfileResponse;

import java.util.List;

public interface UserService {

    UserProfileResponse getCurrentUser();

    List<AdminUserResponse> getAllUsers();

    AdminUserResponse getUserById(Long id);

    AdminUserResponse activateUser(Long id);

    AdminUserResponse deactivateUser(Long id);

    AdminUserResponse updateUserRole(Long id, UpdateUserRoleRequest request);
}
