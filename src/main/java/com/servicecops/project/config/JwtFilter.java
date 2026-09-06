package com.servicecops.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jet.moonlight.services.JetResponse;
import com.servicecops.project.models.database.SystemUserModel;
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
        final String jwt;
        final String userTag;
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        try {

        userTag = jwtUtility.extractUsername(jwt);
        if (userTag != null && SecurityContextHolder.getContext().getAuthentication() == null){
            SystemUserModel userDetails = userDetailsService.loadUserByUsername(userTag);
            if (userDetails == null){
                throw new IllegalStateException("User not found");
            }
            if (jwtUtility.isTokenValid(jwt, userDetails)){
                // check if is_authority_admin and add that permission here
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getWriter(), JetResponse.authFailed("TOKEN EXPIRED"));
            return;
        } catch (Exception e){
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getWriter(), JetResponse.authFailed(e.getMessage()));
            return;
        }

        filterChain.doFilter(request, response);
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
