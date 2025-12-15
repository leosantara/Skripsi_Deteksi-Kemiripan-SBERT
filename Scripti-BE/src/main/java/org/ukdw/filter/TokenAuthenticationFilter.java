package org.ukdw.filter;

import org.hibernate.Hibernate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.ukdw.config.AppProperties;
import org.ukdw.entity.CustomUserDetails;
import org.ukdw.entity.UserEntity;
import org.ukdw.exception.AuthenticationExceptionImpl;
import org.ukdw.exception.InvalidTokenException;
import org.ukdw.services.AuthService;
//import feign.FeignException;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ukdw.services.JwtService;

import java.io.IOException;
import java.util.Collection;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

/**
 * Creator: dendy
 * Date: 6/30/2020
 * Time: 11:59 AM
 * Description :  Contains the logic to call the token validation
 */
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final AuthService authService;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    private final AuthenticationEntryPoint authEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        final String token;
        final String userEmail;
        final String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            token = authHeader.substring(7);
            userEmail = jwtService.extractUserName(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = authService.userDetailsService().loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(token, userDetails)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                } else {
                    throw new InvalidTokenException("invalid JWT Token");
                }
            }
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
//            exception.printStackTrace();
            authEntryPoint.commence(request, response, new AuthenticationExceptionImpl(exception.getMessage()));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Populate excludeUrlPatterns on which one to exclude here
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return appProperties.getExcludeFilter().stream()
                .anyMatch(p -> pathMatcher.match(p, request.getServletPath()));
    }
}
