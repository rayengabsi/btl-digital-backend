package com.btl.digital.mock.controller;

import com.btl.digital.mock.dto.subscription.SubscriptionRequest;
import com.btl.digital.mock.dto.subscription.SubscriptionResponse;
import com.btl.digital.mock.service.MockSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class MockSubscriptionController {

    private final MockSubscriptionService subscriptionService;

    public MockSubscriptionController(MockSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PutMapping("/{subscriptionId}/client-users/{clientUserId}")
    public ResponseEntity<SubscriptionResponse> assign(@PathVariable Long subscriptionId, @PathVariable Long clientUserId) {
        return ResponseEntity.ok(subscriptionService.assignToClient(subscriptionId, clientUserId));
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getAll() {
        return ResponseEntity.ok(subscriptionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(@PathVariable Long id, @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.update(id, request));
    }

    @PostMapping("/{subscriptionId}/permissions/{permissionId}")
    public ResponseEntity<SubscriptionResponse> addPermission(@PathVariable Long subscriptionId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(subscriptionService.addPermission(subscriptionId, permissionId));
    }

    @DeleteMapping("/{subscriptionId}/permissions/{permissionId}")
    public ResponseEntity<SubscriptionResponse> removePermission(@PathVariable Long subscriptionId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(subscriptionService.removePermission(subscriptionId, permissionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}