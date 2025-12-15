package org.ukdw.dto.request.account;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class StatusAccountRequest {
    @NotEmpty(message = "email should not be null or empty")
    private String email;

    private Boolean status;
}