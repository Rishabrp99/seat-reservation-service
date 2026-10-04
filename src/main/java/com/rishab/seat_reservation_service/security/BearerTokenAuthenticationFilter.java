package com.rishab.seat_reservation_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("AUTH DEBUG: " + request.getMethod() + " " + request.getRequestURI()
                + " | Authorization=" + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();

            if (!token.isEmpty()) {
                // For demo/test requirements: "alice-token" -> user "alice"
                String userId = token.endsWith("-token") ? token.replace("-token", "") : token;

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println(
                        "AUTH SET: " +
                                SecurityContextHolder.getContext().getAuthentication()
                );
            }
        }

        filterChain.doFilter(request, response);

        System.out.println(
                "AUTH AFTER CHAIN: " +
                        SecurityContextHolder.getContext().getAuthentication()
        );
    }
}