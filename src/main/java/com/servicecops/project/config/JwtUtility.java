package com.servicecops.project.config;

import com.servicecops.project.models.database.SystemUserModel;
import com.servicecops.project.repositories.SystemUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtUtility {

    private final SystemUserRepository userRepository;

    @Value("${secret}")
    private String secret;

    public static final long JWT_TOKEN_VALIDITY = 12 * 60 * 60;

    public JwtUtility(SystemUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Parses the JWT once and returns all claims. Prefer this on the request path
     * instead of calling extract helpers repeatedly.
     */
    public Claims parseClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Transactional
    public String generateToken(SystemUserModel user) {
        return generateToken(new HashMap<>(), user);
    }

    public boolean isTokenValid(String token, SystemUserModel user) {
        return isTokenValid(parseClaims(token), user);
    }

    public boolean isTokenValid(Claims claims, SystemUserModel user) {
        final String username = claims.getSubject();
        if (username == null || !username.equals(user.getUsername())) {
            return false;
        }
        assertSessionValid(claims, user);
        return true;
    }

    /**
     * Checks expiry, active flag, and token_version (single-session).
     * {@code last_logged_in_at} is audit-only and is not used for revocation.
     */
    private void assertSessionValid(Claims claims, SystemUserModel user) {
        Date expiration = claims.getExpiration();
        if (expiration == null || expiration.before(new Date())) {
            throw new IllegalStateException("SESSION EXPIRED");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("ACCOUNT INACTIVE");
        }
        Object tvClaim = claims.get("tv");
        int tokenVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        if (!(tvClaim instanceof Number number) || number.intValue() != tokenVersion) {
            throw new IllegalStateException("EXPIRED TOKEN USED");
        }
    }

    /**
     * Issues a new JWT for an already-loaded user. Atomically increments
     * {@code tokenVersion} so previous tokens stop working and concurrent logins
     * cannot share a version.
     */
    @Transactional
    public String generateToken(Map<String, Object> claims, SystemUserModel user) {
        long now = System.currentTimeMillis();
        Timestamp loggedInAt = new Timestamp(now);
        int updated = userRepository.incrementTokenVersion(user.getId(), loggedInAt);
        if (updated != 1) {
            throw new IllegalStateException("Failed to bump token_version for user " + user.getId());
        }
        SystemUserModel refreshed = userRepository.getRequired(user.getId());
        int nextVersion = refreshed.getTokenVersion() == null ? 0 : refreshed.getTokenVersion();
        user.setTokenVersion(nextVersion);
        user.setLastLoggedInAt(refreshed.getLastLoggedInAt());

        claims.put("role", user.getRoleName());
        claims.put("role_code", user.getRoleCode());
        claims.put("domain", user.getRoleDomain() == null ? null : user.getRoleDomain().name());
        claims.put("tv", nextVersion);
        List<String> permissions = new ArrayList<>();
        for (GrantedAuthority authority : user.getAuthorities()) {
            String code = authority.getAuthority();
            if (!permissions.contains(code)) {
                permissions.add(code);
            }
        }
        claims.put("permissions", permissions);
        return Jwts
                .builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
