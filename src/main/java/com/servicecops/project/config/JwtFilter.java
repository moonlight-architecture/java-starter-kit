package com.servicecops.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jet.moonlight.services.JetResponse;
import com.servicecops.project.models.database.SystemUserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtility jwtUtility;
    private final ApplicationConf userDetailsService;
    private final ObjectMapper mapper;

    @Value("${jet.docs.path:/docs}")
    private String docsPath;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7);
        try {
            Claims claims = jwtUtility.parseClaims(jwt);
            String userTag = claims.getSubject();
            if (userTag != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SystemUserModel userDetails = userDetailsService.loadUserByUsername(userTag);
                if (userDetails == null) {
                    writeAuthFailed(response);
                    return;
                }
                if (jwtUtility.isTokenValid(claims, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (ExpiredJwtException e) {
            writeAuthFailed(response);
            return;
        } catch (Exception e) {
            writeAuthFailed(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeAuthFailed(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), JetResponse.authFailed("USER AUTHENTICATION FAILED"));
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String docs = (docsPath == null || docsPath.isBlank()) ? "/docs" : docsPath.trim();
        if (!docs.startsWith("/")) {
            docs = "/" + docs;
        }
        if (docs.length() > 1 && docs.endsWith("/")) {
            docs = docs.substring(0, docs.length() - 1);
        }
        return path.equals(docs) || path.startsWith(docs + "/");
    }
}
