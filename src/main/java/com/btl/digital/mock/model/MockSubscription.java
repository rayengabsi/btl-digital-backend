package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * A subscription offering a set of permission authorities.
 */
@Data
@Builder
public class MockSubscription {

    private Long id;
    private String name;
    private String description;
    private Set<Long> permissionIds;
}