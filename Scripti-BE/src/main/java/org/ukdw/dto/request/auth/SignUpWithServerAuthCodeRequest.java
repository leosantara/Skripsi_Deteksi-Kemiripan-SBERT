package org.ukdw.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Creator: dendy
 * Date: 8/5/2020
 * Time: 11:17 AM
 * Description : request dto for sign up
 */
@Data
public class SignUpWithServerAuthCodeRequest {

    @NotEmpty(message = "serverAuthCode should not be null or empty.")
    @JsonProperty("serverAuthCode")
    private String serverAuthCode;

    //registration number
    @NotEmpty(message = "regNumber should not be null or empty.")
    @JsonProperty("regNumber")
    private String regNumber;

    @NotEmpty(message = "role should not be null or empty.")
    @JsonProperty("role")
    private String role;

    @NotEmpty(message = "clientType should not be null or empty. Choose mobile_app or web_app")
    @JsonProperty("clientType")
    private String clientType;
}


