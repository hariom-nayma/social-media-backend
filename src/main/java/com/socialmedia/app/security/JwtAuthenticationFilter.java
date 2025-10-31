package com.socialmedia.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.socialmedia.app.enums.TokenType;
import com.socialmedia.app.util.JwtUtil;

import java.io.IOException;


import com.socialmedia.app.service.SessionService;
import com.socialmedia.app.service.JwtBlacklistService;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final SessionService sessionService;
    private final JwtBlacklistService jwtBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, SessionService sessionService, JwtBlacklistService jwtBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String token = header.substring(7);
        try {
            if (jwtBlacklistService.isBlacklisted(token)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!jwtUtil.isTokenType(token,TokenType.ACCESS)) {
                filterChain.doFilter(request, response);
                return;
            }
//            System.err.println("Path : " + request.getRequestURI());
//            System.err.println("S Path : " + request.getServletPath());

            String username = jwtUtil.getUsername(token);
            var userDetails = userDetailsService.loadUserByUsername(username);
            var authorities = userDetails.getAuthorities();
            var auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            sessionService.updateLastActiveTime(token);
        } catch (Exception ex) {
            // log and continue — SecurityContext remains empty and request will be rejected if protected
        }

        filterChain.doFilter(request, response);
    }
}
