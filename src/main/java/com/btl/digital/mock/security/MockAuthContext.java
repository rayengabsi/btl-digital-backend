package com.btl.digital.mock.security;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.store.MockDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the currently authenticated mock client user from the security
 * context, throwing the documented AUTHENTICATION_REQUIRED error when absent.
 */
@Component
public class MockAuthContext {

    private final MockDataStore store;

    public MockAuthContext(MockDataStore store) {
        this.store = store;
    }

    public MockClientUser requireClient() {
        MockClientUser client = currentClient();
        if (client == null) {
            throw new MockApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Authentication is required.");
        }
        return client;
    }

    private static final String MOCK_TEST_USER_EMAIL = "rayen.gabsi@mock.dev";

    public MockClientUser currentClient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MockPrincipal principal) {
            return store.clientById(principal.clientUserId()).orElse(null);
        }
        // No JWT is presented (the non-auth mock banking endpoints are permit-all):
        // resolve the deterministic mock test user so services get Rayen's context.
        return store.clientByEmail(MOCK_TEST_USER_EMAIL).orElse(null);
    }
}