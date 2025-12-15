package org.ukdw.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Creator: dendy
 * Date: 7/11/2020
 * Time: 12:55 PM
 * Description : request for SignIn
 * server authcode are come from google sign in response
 */
@Data
public class SignInRequest {
    @NotEmpty(message = "serverAuthCode should not be null or empty")
    private String serverAuthCode;

    @NotEmpty(message = "clientType should not be null or empty. Choose mobile_app or web_app")
    private String clientType;
}
