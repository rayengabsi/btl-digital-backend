package com.btl.digital.mock.security;

/**
 * The authenticated mock client user principal stored in the security context.
 */
public record MockPrincipal(Long clientUserId, String email) {
}
