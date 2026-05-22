package com.unext.backend.user.dto;

import com.unext.backend.user.entity.UserRole;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 8, max = 100)
        String password,

        UserRole role,

        Boolean active
) {}
