package com.zetasoft.api.controller;

import com.zetasoft.api.model.dto.LoginRequest;
import com.zetasoft.api.model.dto.LoginResponse;
import com.zetasoft.api.model.dto.PasswordResetConfirmRequest;
import com.zetasoft.api.model.dto.PasswordResetRequest;
import com.zetasoft.api.model.dto.EmailVerificationRequest;
import com.zetasoft.api.model.dto.EmailVerificationConfirmRequest;
import com.zetasoft.api.security.JwtHelper;
import com.zetasoft.api.service.PasswordResetService;
import com.zetasoft.api.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization operations")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "User login", description = "Authenticate user and obtain JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, token returned",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtHelper.generateToken(userDetails);

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(request.getUsername())
                .expiresIn(jwtHelper.getExpiration())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Request password reset", description = "Request a password reset email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping("/password-reset")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Confirm password reset", description = "Reset password using token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Request email verification", description = "Request an email verification token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification email sent"),
            @ApiResponse(responseCode = "400", description = "Email already verified", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping("/email-verification")
    public ResponseEntity<Void> requestEmailVerification(@Valid @RequestBody EmailVerificationRequest request) {
        emailVerificationService.requestVerification(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Confirm email verification", description = "Verify email using token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping("/email-verification/confirm")
    public ResponseEntity<Void> confirmEmailVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
        emailVerificationService.confirmVerification(request.getToken());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
