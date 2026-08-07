package com.btl.digital.mock.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}