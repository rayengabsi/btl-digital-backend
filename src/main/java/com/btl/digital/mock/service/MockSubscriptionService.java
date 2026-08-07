package com.btl.digital.mock.service;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.dto.subscription.SubscriptionRequest;
import com.btl.digital.mock.dto.subscription.SubscriptionResponse;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.model.MockSubscription;
import com.btl.digital.mock.security.MockAuthContext;
import com.btl.digital.mock.store.MockDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;

@Service
public class MockSubscriptionService {

    private final MockDataStore store;
    private final MockAuthContext authContext;

    public MockSubscriptionService(MockDataStore store, MockAuthContext authContext) {
        this.store = store;
        this.authContext = authContext;
    }

    public SubscriptionResponse assignToClient(Long subscriptionId, Long clientUserId) {
        requireClientAuthenticated();
        requirePermission("SET_SUBSCRIPTION");
        store.subscriptionById(subscriptionId)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Subscription not found."));
        MockClientUser user = store.clientById(clientUserId)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Client user not found."));
        user.setSubscriptionId(subscriptionId);
        return subResponse(store.subscriptionById(subscriptionId).orElseThrow());
    }

    public SubscriptionResponse create(SubscriptionRequest request) {
        requireClientAuthenticated();
        requirePermission("MANAGE_SUBSCRIPTION");
        MockSubscription sub = MockSubscription.builder()
                .id(store.nextSubscriptionId())
                .name(request.getName())
                .description(request.getDescription())
                .permissionIds(new LinkedHashSet<>())
                .build();
        store.addSubscription(sub);
        return subResponse(sub);
    }

    public List<SubscriptionResponse> getAll() {
        requireClientAuthenticated();
        requirePermission("SELECT_SUBSCRIPTION");
        return store.allSubscriptions().stream().map(this::subResponse).toList();
    }

    public SubscriptionResponse get(Long id) {
        requireClientAuthenticated();
        requirePermission("SELECT_SUBSCRIPTION");
        return store.subscriptionById(id)
                .map(this::subResponse)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Subscription not found."));
    }

    public SubscriptionResponse update(Long id, SubscriptionRequest request) {
        requireClientAuthenticated();
        requirePermission("MANAGE_SUBSCRIPTION");
        MockSubscription sub = store.subscriptionById(id)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Subscription not found."));
        sub.setName(request.getName());
        sub.setDescription(request.getDescription());
        store.addSubscription(sub);
        return subResponse(sub);
    }

    public SubscriptionResponse addPermission(Long subscriptionId, Long permissionId) {
        requireClientAuthenticated();
        requirePermission("MANAGE_PERMISSION");
        MockSubscription sub = store.subscriptionById(subscriptionId)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Subscription not found."));
        store.permissionById(permissionId)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Permission not found."));
        sub.getPermissionIds().add(permissionId);
        store.addSubscription(sub);
        return subResponse(sub);
    }

    public SubscriptionResponse removePermission(Long subscriptionId, Long permissionId) {
        requireClientAuthenticated();
        requirePermission("MANAGE_PERMISSION");
        MockSubscription sub = store.subscriptionById(subscriptionId)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Subscription not found."));
        store.permissionById(permissionId)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Permission not found."));
        sub.getPermissionIds().remove(permissionId);
        store.addSubscription(sub);
        return subResponse(sub);
    }

    public void delete(Long id) {
        requireClientAuthenticated();
        requirePermission("MANAGE_SUBSCRIPTION");
        store.subscriptionById(id)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Subscription not found."));
        store.removeSubscription(id);
    }

    private SubscriptionResponse subResponse(MockSubscription sub) {
        List<String> codes = sub.getPermissionIds().stream()
                .map(store::permissionById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(p -> p.getCode())
                .toList();
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .name(sub.getName())
                .description(sub.getDescription())
                .permissions(codes)
                .build();
    }

    private void requireClientAuthenticated() {
        authContext.requireClient();
    }

    private void requirePermission(String permission) {
        MockClientUser client = authContext.requireClient();
        if (!store.permissionsGrantedToClient(client.getId()).contains(permission)) {
            throw new MockApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "User does not have the required permission: " + permission);
        }
    }
}