package com.btl.digital.mock.service;

import java.math.BigInteger;

/**
 * Validates a Tunisian RIB using the modulo-97 algorithm on its 20 digits.
 */
public final class RibValidator {

    private RibValidator() {
    }

    public static boolean isValid(String rib) {
        if (rib == null || rib.length() != 20 || !rib.chars().allMatch(Character::isDigit)) {
            return false;
        }
        String body = rib.substring(0, 18) + "00";
        BigInteger value = new BigInteger(body);
        BigInteger remainder = value.mod(BigInteger.valueOf(97));
        BigInteger key = BigInteger.valueOf(97).subtract(remainder);
        return String.format("%02d", key.intValue()).equals(rib.substring(18));
    }
}