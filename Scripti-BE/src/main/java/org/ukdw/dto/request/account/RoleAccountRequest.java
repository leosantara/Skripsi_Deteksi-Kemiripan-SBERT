package org.ukdw.dto.request.account;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RoleAccountRequest {
    @NotEmpty(message = "email should not be null or empty")
    private String email;

    private List<Long> groupIds; // menambahkan list group IDs
}
