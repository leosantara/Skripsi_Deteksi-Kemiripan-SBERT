package org.ukdw.dto.request.account;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Creator: dendy
 * Date: 7/11/2020
 * Time: 12:52 PM
 * Description : Profile Request
 */

@Data
public class AccountProfileRequest {
    @NotEmpty(message = "email should not be null or empty")
    private String email;
}
