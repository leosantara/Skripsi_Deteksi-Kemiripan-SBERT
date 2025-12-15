package org.ukdw.dto.request.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Creator: dendy
 * Date: 7/11/2020
 * Time: 12:52 PM
 * Description : VerifyAccessToken Request
 */

@Data
public class VerifyAccessTokenRequest {
    @NotEmpty(message = "accessToken should not be null or empty")
    private String accessToken;
}
