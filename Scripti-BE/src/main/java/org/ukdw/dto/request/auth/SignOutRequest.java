package org.ukdw.dto.request.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Creator: dendy
 * Date: 8/5/2020
 * Time: 11:17 AM
 * Description : request dto sign out
 */
@Data
public class SignOutRequest {
    @NotEmpty(message = "accessToken should not be null or empty")
    private String accessToken;
}