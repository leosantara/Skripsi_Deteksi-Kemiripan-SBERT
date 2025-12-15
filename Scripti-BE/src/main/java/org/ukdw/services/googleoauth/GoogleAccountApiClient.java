package org.ukdw.services.googleoauth;

//import feign.FeignException;
import feign.FeignException;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.ukdw.dto.response.AccessTokenResponse;

import java.util.Map;

/**
 * User: dendy
 * Date: 29/08/2020
 * Time: 7:53
 * Description : GoogleApiClient using feign
 */
@FeignClient(name = "oauth2googleapi", url = "https://accounts.google.com/o/oauth2")
public interface GoogleAccountApiClient {

    // revoke google user using valid access token or refresh token
    // get https://accounts.google.com/o/oauth2/revoke?token={token}
    @Headers("Content-type: application/x-www-form-urlencoded")
    @PostMapping(value = "/revoke")
    AccessTokenResponse revokeToken(@RequestHeader Map<String, String> headerMap,
                                    @RequestParam("token") String token) throws FeignException;
}