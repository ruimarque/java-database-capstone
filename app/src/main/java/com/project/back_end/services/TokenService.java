package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.print.Doc;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String secret;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository
    ) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String identifier) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(identifier)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(7, ChronoUnit.DAYS))) // 7 days expiration
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
// 5. **extractEmail Method**
// This method extracts the user's email (subject) from the provided JWT token.
// - The token is first verified using the signing key to ensure it hasn’t been tampered with.
// - After verification, the token is parsed, and the subject (which represents the email) is extracted.
// This method allows the application to retrieve the user's identity (email) from the token for further use.

    public boolean validateToken(String token, String role) {
        try {
            String identifier = this.extractEmail(token);
            if(role.equalsIgnoreCase("admin")) {
                return adminRepository.findByUsername(identifier) != null;
            } else if(role.equalsIgnoreCase("doctor")) {
                return doctorRepository.findByEmail(identifier) != null;
            } else if(role.equalsIgnoreCase("patient")) {
                return patientRepository.findByEmail(identifier) != null;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
