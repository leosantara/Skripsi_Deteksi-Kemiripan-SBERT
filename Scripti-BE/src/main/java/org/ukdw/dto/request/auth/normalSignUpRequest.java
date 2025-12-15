package org.ukdw.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * Creator: dendy
 * Date: 8/5/2020
 * Time: 11:17 AM
 * Description : request dto for normal sign up. this type of sign up require no authcode.
 */
@Data
public class normalSignUpRequest {

    @NotEmpty(message = "email should not be null or empty")
    @JsonProperty("email")
    private String email;

    //registration number
    @NotEmpty(message = "regNumber should not be null or empty")
    @JsonProperty("regNumber")
    private String regNumber;

    @NotEmpty(message = "role should not be null or empty")
    @JsonProperty("role")
    private String role;
}