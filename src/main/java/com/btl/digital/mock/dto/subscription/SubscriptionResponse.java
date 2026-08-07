package com.btl.digital.mock.dto.subscription;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SubscriptionResponse {
    private Long id;
    private String name;
    private String description;
    private List<String> permissions;
}