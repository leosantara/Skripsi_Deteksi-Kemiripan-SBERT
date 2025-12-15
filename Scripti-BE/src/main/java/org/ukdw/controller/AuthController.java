package org.ukdw.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.ukdw.dto.UserDTO;
import org.ukdw.dto.request.auth.*;
import org.ukdw.dto.response.ResponseWrapper;
import org.ukdw.dto.response.auth.RefreshAccessTokenResponse;
import org.ukdw.dto.user.SignInWithEmailRequest;
import org.ukdw.entity.*;
import org.ukdw.exception.InvalidTokenException;
import org.ukdw.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ukdw.services.UserAccountService;

import java.text.ParseException;

/**
 * Creator: dendy
 * Date: 7/11/2020
 * Time: 12:52 PM
 * Description : REST controller for authentication API
 */

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserAccountService userAccountService;
    private final ModelMapper modelMapper;

    @ResponseBody
    @Operation(summary = "Sign in with SignInRequest")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signin successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignInRequest.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")})
    @PostMapping(value = "/signin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Signin using google server authcode", required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignInRequest.class),
                            examples = @ExampleObject(
                                    value = "{ \"serverAuthCode\": \"serverauthcode from google\", " +
                                            "\"clientType\": \"web_app\" }"
                            )
                    )
            )
            @Valid @RequestBody SignInRequest request) {
        UserEntity userEntity = authService.signIn(request.getServerAuthCode(), request.getClientType());
        ResponseWrapper<UserEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), userEntity);
        return ResponseEntity.ok(response);
    }

    @Hidden
    @ResponseBody
    @Operation(summary = "Mendaftar user baru menggunakan server Auth code (Not Tested Yet!)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signin successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignUpWithServerAuthCodeRequest.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")})
    @PostMapping(value = "/signupwithgoogleauthCode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signUpWithServerAuthCode(
            @Valid @RequestBody SignUpWithServerAuthCodeRequest request) throws InvalidTokenException {
        boolean isSucceed = authService.signUpWithGoogleAuthCode(
                request.getServerAuthCode(),
                request.getRegNumber(),
                request.getRole(),
                request.getClientType()
        );
        ResponseWrapper<Boolean> response = new ResponseWrapper<>(HttpStatus.OK.value(), isSucceed);
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @Operation(summary = "Daftar User Baru")
    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signUp(@Valid @RequestBody normalSignUpRequest request) throws InvalidTokenException {
        boolean isSucceed = authService.normalSignUp(
                request.getEmail(),
                request.getRegNumber(),
                request.getRole()
        );
        ResponseWrapper<Boolean> response = new ResponseWrapper<>(HttpStatus.OK.value(), isSucceed);
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @Operation(summary = "Log Out")
    @PostMapping(value = "/signout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signout(@Valid @RequestBody SignOutRequest request) {
        boolean isSucceed = authService.signOut(request.getAccessToken());
        ResponseWrapper<Boolean> response = new ResponseWrapper<>(HttpStatus.OK.value(), isSucceed);
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @Operation(summary = "Veritifikasi Access Token")
    @PostMapping(value = "/verifyaccesstoken", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verifyAccessToken(@Valid @RequestBody VerifyAccessTokenRequest request) {
        boolean isSucceed = authService.validateAccessToken(request.getAccessToken());
        ResponseWrapper<Boolean> response = new ResponseWrapper<>(HttpStatus.OK.value(), isSucceed);
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @Operation(summary = "Meminta Acces Token")
    @PostMapping(value = "/refreshaccesstoken", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refreshAccessToken(@Valid @RequestBody RefreshAccessTokenRequest request)
            throws InvalidTokenException, ParseException {
        RefreshAccessTokenResponse refreshAccessTokenResponse = authService.refreshAccessToken(request.getRefreshToken());
        ResponseWrapper<RefreshAccessTokenResponse> response = new ResponseWrapper<>(HttpStatus.OK.value(), refreshAccessTokenResponse);
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @Operation(summary = "Memutus Accces Token")
    @PostMapping(value = "/revoke", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> googleRevokeToken(@Valid @RequestBody RevokeTokenRequest request) {
        boolean status = authService.revokeGoogleToken(request.getToken());
        ResponseWrapper<Boolean> response = new ResponseWrapper<>(HttpStatus.OK.value(), status);
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @Operation(summary = "Logout dengan revoke Accces Token")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String bearerToken) {
        // Mengambil hanya token dari "Bearer {token}"
        String token = bearerToken.replace("Bearer ", "");
        // Panggil service signOut untuk revoke token
        boolean isLoggedOut = authService.signOut(token);
        ResponseWrapper<Boolean> response = new ResponseWrapper<>(HttpStatus.OK.value(), isLoggedOut);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/signin-email", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "melakukan signin dengan email")
    public ResponseEntity<?> signInWithEmail(
            @Valid @RequestBody SignInWithEmailRequest request) {

        // Panggil service untuk sign in dengan email
        UserEntity userEntity = authService.signInWithEmail(request.getEmail());

        // Bungkus response dengan objek ResponseWrapper
        ResponseWrapper<UserEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), userEntity);

        // Kembalikan response dengan status OK
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @Operation(summary = "Menambah user")
    @PostMapping(value = "/add-user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUserAccount(@RequestBody UserDTO userDTO) {
        // Mengkonversi UserDTO ke UserEntity
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);

        // Memanggil service untuk menyimpan userEntity
        UserEntity createdUser = userAccountService.createUserAccount(userEntity);

        if (createdUser != null) {
            ResponseWrapper<UserEntity> response = new ResponseWrapper<>(
                    HttpStatus.CREATED.value(), "User berhasil ditambahkan", createdUser);
            return ResponseEntity.ok(response);
        } else {
            ResponseWrapper<String> response = new ResponseWrapper<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "Terjadi kesalahan saat menambahkan user", null);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Mencari dosen berdsarkan NIDN")
    @GetMapping("/find-dosen/{nidn}")
    public ResponseEntity<?> findByNidn(@PathVariable String nidn) {
        DosenEntity userData = userAccountService.findByNIDN(nidn);
        ResponseWrapper<DosenEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), userData);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mencari mahasiswa berdasarkan NIM")
    @GetMapping("/find-mahasiswa/{nim}")
    public ResponseEntity<?> findByNim(@PathVariable String nim) {
        MahasiswaEntity userData = userAccountService.findByNIM(nim);
        ResponseWrapper<MahasiswaEntity> response = new ResponseWrapper<>(HttpStatus.OK.value(), userData);
        return ResponseEntity.ok(response);
    }

}
