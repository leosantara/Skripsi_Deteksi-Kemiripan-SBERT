/**
 * Author: dendy
 * Date:03/10/2024
 * Time:11:46
 * Description:
 */

package org.ukdw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ukdw.dto.UserDTO;
import org.ukdw.dto.request.account.AccountProfileRequest;
import org.ukdw.dto.response.ResponseWrapper;
import org.ukdw.entity.DosenEntity;
import org.ukdw.entity.MahasiswaEntity;
import org.ukdw.entity.UserEntity;
import org.ukdw.services.UserAccountService;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-key")
@RequestMapping("/account")
public class AccountController {
    private final UserAccountService userAccountService;

    @Operation(summary = "Mendapatkan user profile")
    @PostMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAccountProfile(@Valid @RequestBody AccountProfileRequest request) {
        UserEntity userEntity = userAccountService.getUserAccountByEmail(request.getEmail());
        ResponseWrapper<UserEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), userEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/find-user/{nimOrNidn}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findByNimOrNidn(@PathVariable String nimOrNidn) {
        Object userData = userAccountService.findByNimOrNidn(nimOrNidn);
        ResponseWrapper<Object> response = new ResponseWrapper<>(HttpStatus.OK.value(), userData);
        return ResponseEntity.ok(response);
    }
}
