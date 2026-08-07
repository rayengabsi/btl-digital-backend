package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

/**
 * A fake bank customer as returned by the fake "T24" backend used during
 * registration client verification and transfer profit-centre metadata.
 */
@Data
@Builder
public class MockCustomer {

    private String customerId;
    private String firstName;
    private String lastName;
    /** CIN or PASSPORT */
    private String idType;
    private String idNumber;
    /** yyyyMMdd */
    private String dateOfBirth;
    /** E.164 phone, e.g. 21655123456 */
    private String phoneNumber;
    private String email;
    private boolean registered;
    /** Pre-registered client user id if the customer already has a digital account */
    private Long clientUserId;
}