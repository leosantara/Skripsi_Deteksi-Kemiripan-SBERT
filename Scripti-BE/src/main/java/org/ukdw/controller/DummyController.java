package org.ukdw.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ukdw.dto.response.ResponseWrapper;
import org.ukdw.entity.UserEntity;
import org.ukdw.services.UserAccountService;

import java.util.List;

//@Hidden
@RestController
@RequiredArgsConstructor
public class DummyController {

    private final UserAccountService userAccountService;

    //dummy sample
    @GetMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> hello() {
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Hello from public API");
        return ResponseEntity.ok(response);
    }

    //dummy sample
    @SecurityRequirement(name = "bearer-key")
    @GetMapping(value = "/restricted", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> restricted() {
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Hello from restricted API");
        return ResponseEntity.ok(response);
    }

    // Restricted endpoint, accessible only to users with the role "ADMIN"
    @PreAuthorize("hasRole(T(org.ukdw.entity.AuthoritiesConstants).ROLE_ADMINISTRATOR.name())")
    @GetMapping(value = "/adminonly", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> adminonly() {
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Hello from admin only API");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(org.ukdw.entity.AuthoritiesConstants).ROLE_DOSEN.name())")
    @GetMapping("/dosenonly")
    public ResponseEntity<?> dosenonly() {
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Hello from dosen only API");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(org.ukdw.entity.AuthoritiesConstants).ROLE_MAHASISWA.name())")
    @GetMapping("/mahasiswaonly")
    public ResponseEntity<?> mahasiswaonly() {
        ResponseWrapper<String> response = new ResponseWrapper<>(HttpStatus.OK.value(), "Hello from mahasiswa only API");
        return ResponseEntity.ok(response);
    }
}
