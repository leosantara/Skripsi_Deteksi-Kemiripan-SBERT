package org.ukdw.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import feign.FeignException;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.ukdw.dto.response.AccessTokenResponse;
import org.ukdw.dto.response.auth.RefreshAccessTokenResponse;
import org.ukdw.dto.user.UserRoleDTO;
import org.ukdw.entity.*;
import org.ukdw.entity.UserEntity;
import org.ukdw.exception.AuthenticationExceptionImpl;
import org.ukdw.exception.BadRequestException;
import org.ukdw.exception.ScNotFoundException;
import org.ukdw.exception.InvalidTokenException;
import org.ukdw.filter.EmailValidation;
import org.ukdw.repository.GoogleUserRepository;
import org.ukdw.repository.UserRepository;
import org.ukdw.services.googleoauth.GoogleAccountApiClient;
import org.ukdw.services.googleoauth.GoogleApiClient;
import org.ukdw.util.GoogleTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Creator: dendy
 * Date: 8/29/2020
 * Time: 7:52 AM
 * Description : service for auth process
 */

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    private final UserRoleService roleService;
    private final EmailValidation emailValidation;

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserAccountService userAccountService;

    private final GoogleApiClient googleApiClient;

    private final GoogleAccountApiClient googleAccountApiClient;

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final GoogleUserRepository googleUserRepository;

    //todo: need to refactor
    public boolean signUpWithGoogleAuthCode(String authCode, String regNumber, String role, String clientType)
            throws InvalidTokenException {
        if (!regNumber.isEmpty() || !role.isEmpty()) {
            UserRoleDTO userRole = roleService.getRole(role);
            GoogleTokenResponse response;
            GoogleIdToken.Payload payload;
            try {
                response = googleTokenVerifier.verifyAuthCode(authCode, clientType);
                payload = response.parseIdToken().getPayload();
            } catch (IOException e) {
                throw new InvalidTokenException("invalid authcode");
            }
            return !googleTokenVerifier.verifyIdToken(response.getIdToken()).getEmail().isEmpty();
//            if (emailValidation.emailIsValid(payload.getEmail())) {
//                return userAccountService.addUserAccount(payload.getEmail(), regNumber, userRole,
//                        response.getRefreshToken());
//            }
//            throw new UnauthorizedException("Email must be ti, si or staff");
        }
        throw new BadRequestException("Sorry! NIM or Role can't empty");
    }

    //todo: need to refactor
    public boolean normalSignUp(String email, String nomorInduk, String role) throws InvalidTokenException {
       /* if (!nomorInduk.isEmpty() || !role.isEmpty()) {
            UserRoleDTO userRole = roleService.getRole(role);
            if (emailValidation.emailIsValid(email)) {
                return userAccountService.addUserAccount(email, nomorInduk, userRole, null);
            }
            throw new UnauthorizedException("Email must be ti, si or staff");
        }*/
        throw new BadRequestException("Sorry! NIM or Role can't empty");
    }

    public boolean signOut(String accessToken) {
        try {
            // Verifikasi access token
            AccessTokenResponse verifyAccessTokenResponse = googleApiClient.verifyAccessToken(accessToken);

            // Dapatkan detail akun pengguna
            UserEntity userAccount = userAccountService.getUserAccountByEmail(verifyAccessTokenResponse.getEmail());
            GoogleUserEntity googleUser = googleUserRepository.findByGoogleEmail(verifyAccessTokenResponse.getEmail());

            if (googleUser != null) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);

                // Revoke the access token
                AccessTokenResponse revokeTokenResponse = googleAccountApiClient.revokeToken(headers, accessToken);
                if (revokeTokenResponse != null) {
                    // Set access token, refresh token, dan id token menjadi null pada table google user dan user
                    googleUser.setAccessToken(null);
                    googleUser.setRefreshToken(null);
                    googleUser.setIdToken(null);
                    userAccount.setJwtToken(null);

                    // Simpan perubahan ke database
                    googleUserRepository.save(googleUser);
                    userRepository.save(userAccount);
                    return true; // Berhasil sign out
                }
            }
        } catch (FeignException e) {
            return false;
        }
        return false;
    }


    //public ResponseEntity<?> addUser()


    public UserEntity signIn(String authCode, String clientType) throws ScNotFoundException, BadRequestException {
        try {
            // Verifikasi kode otentikasi Google (hanya sekali)
            GoogleTokenResponse response = googleTokenVerifier.verifyAuthCode(authCode, clientType);
            GoogleIdToken.Payload payload = response.parseIdToken().getPayload();

            // Ambil email dari payload
            String googleEmail = payload.getEmail();

            // Validasi email menggunakan emailValidation.emailIsValid()
            if (!emailValidation.emailIsValid(googleEmail)) {
                throw new AuthenticationExceptionImpl("Only @ti.ukdw.ac.id, @si.ukdw.ac.id, or staff emails are allowed");
            }

            // Cek apakah email Google sudah terdaftar di database
            GoogleUserEntity googleUser = googleUserRepository.findByGoogleEmail(googleEmail);

            // Jika pengguna belum terdaftar, daftarkan (logika dari signUpGoogle)
            if (googleUser == null) {
                googleUser = new GoogleUserEntity();
                googleUser.setGoogleId(payload.getSubject()); // ID pengguna unik Google
                googleUser.setGoogleName((String) payload.get("name"));
                googleUser.setGoogleEmail(googleEmail);
                googleUser.setGoogleLink(payload.get("profile") != null ? (String) payload.get("profile") : ""); // Setel URL profil
                googleUser.setGooglePictureLink((String) payload.get("picture"));
                googleUser.setAccessToken(response.getAccessToken());
                googleUser.setIdToken(""); // Atur sesuai kebutuhan
                googleUser.setRefreshToken(response.getRefreshToken());

                // Simpan pengguna Google baru di database
                googleUserRepository.save(googleUser);
            }

            // Cek apakah user dengan email Google yang terdaftar sudah ada di tabel UserEntity
            UserEntity userEntity = userRepository.findByGoogleUser(googleUser);
            if (userEntity == null) {
                // Jika pengguna tidak ditemukan di UserEntity, lempar exception
                throw new AuthenticationExceptionImpl("User not registered. Username: " + googleUser.getGoogleEmail());
            }

            // Perbarui token akses Google dan setel refresh token, id token, dan access token
            googleUser.setIdToken(response.getIdToken());
            googleUser.setRefreshToken(response.getRefreshToken());
            googleUser.setAccessToken(response.getAccessToken());

            // Simpan perubahan GoogleUser ke dalam UserEntity
            userEntity.setGoogleUser(googleUser);

            // Buat token JWT menggunakan informasi pengguna yang ada
            CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
            String jwtToken = jwtService.generateToken(customUserDetails);
            userEntity.setJwtToken(jwtToken);

            // Kembalikan userEntity yang berhasil login
            return userEntity;
        } catch (IOException e) {
            throw new BadRequestException("Sign-in or Sign-up failed: " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse the response");
        }
    }



    public String validateIdToken(String idToken) throws InvalidTokenException {
        if (idToken != null) {
            return googleTokenVerifier.verifyIdToken(idToken).getEmail();
        }
        throw new BadRequestException("Id Token cant be empty");
    }

    public boolean validateAccessToken(String accessToken) {
//        try {
//            AccessTokenResponse accessTokenResponse = googleApiClient.verifyAccessToken(accessToken);
//            return accessTokenResponse != null;
//        } catch (FeignException e) {
        return false;
//        }
    }

    //todo: need to refactor
    public RefreshAccessTokenResponse refreshAccessToken(String refreshToken) throws InvalidTokenException, ParseException {
        try {
            //refresh accsess token
            GoogleTokenResponse googleTokenResponse = googleTokenVerifier.refreshAccessToken(refreshToken);
            GoogleIdToken.Payload payload = googleTokenResponse.parseIdToken().getPayload();
            //check if google email are registered in DB
            GoogleUserEntity googleUserEntity = googleUserRepository.findByGoogleEmail(payload.getEmail());
            //check if user with registered google email already available
            if(googleUserEntity == null) {
                throw new AuthenticationExceptionImpl("email not registered. email :"
                        + payload.getEmail());
            }
            UserEntity userEntity = userRepository.findByGoogleUser(googleUserEntity);
            if (userEntity == null) {
                throw new AuthenticationExceptionImpl("User not registered. username :"
                        + googleUserEntity.getGoogleName());
            }
            googleUserEntity.setGoogleEmail(payload.getEmail());
            googleUserEntity.setAccessToken(googleTokenResponse.getAccessToken());
            googleUserEntity.setIdToken(googleTokenResponse.getIdToken());
            googleUserEntity.setRefreshToken(googleTokenResponse.getRefreshToken());
            userEntity.setGoogleUser(googleUserEntity);
            // generate jwt access token using given user info
            CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
            String token = jwtService.generateToken(customUserDetails);
            userEntity.setJwtToken(token);

            return new RefreshAccessTokenResponse(token, googleTokenResponse.getIdToken());
        } catch (IOException | ParseException e) {
            throw new AuthenticationExceptionImpl("Refresh access token Failed : " + e.getMessage());
        }
    }

    public boolean revokeGoogleToken(String token) {
        try {

//            //token can be access token or refresh token
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
            AccessTokenResponse accessTokenResponse = googleAccountApiClient.revokeToken(headers, token);
            return accessTokenResponse != null;
        } catch (FeignException e) {
            return false;
        }
    }



    public UserDetailsService userDetailsService() {
        return username -> {
            UserEntity userEntity = userRepository.findByUsername(username);
            if (userEntity == null) {
                throw new UsernameNotFoundException("User " + username + " not found");
            }
            CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
            return customUserDetails;
        };
    }

    public UserEntity signInWithEmail(String email) throws ScNotFoundException, BadRequestException {
        try {

            // Cek apakah email Google sudah terdaftar di tabel google_user
            GoogleUserEntity googleUser = googleUserRepository.findByGoogleEmail(email);
            if (googleUser == null) {
                throw new AuthenticationExceptionImpl("Email not registered in Google User: " + email);
            }

            // Cek apakah user dengan email Google yang terdaftar sudah ada di tabel user
            UserEntity userEntity = userRepository.findByGoogleUser(googleUser);
            if (userEntity == null) {
                throw new AuthenticationExceptionImpl("User not registered in the system. Username: " + googleUser.getGoogleName());
            }

            // Buat token JWT menggunakan informasi pengguna yang ada
            CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
            String jwtToken = jwtService.generateToken(customUserDetails);
            userEntity.setJwtToken(jwtToken);

            // Kembalikan userEntity yang berhasil login
            return userEntity;
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse the response");
        }
    }



}
