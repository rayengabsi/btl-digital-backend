package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

/**
 * A permission authority assignable to subscriptions.
 */
@Data
@Builder
public class MockPermission {

    private Long id;
    private String code;
}