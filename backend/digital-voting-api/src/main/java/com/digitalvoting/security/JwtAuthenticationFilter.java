package com.digitalvoting.security;

import com.digitalvoting.entity.Voter;
import com.digitalvoting.repository.VoterRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final VoterRepository voterRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            VoterRepository voterRepository) {

        this.jwtService = jwtService;
        this.voterRepository = voterRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        System.out.println(
                "JWT Filter: " + request.getRequestURI()
        );

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            System.out.println("JWT Filter: No Bearer token");

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(7);

        try {

            String voterId =
                    jwtService.extractVoterId(token);

            if (!jwtService.isTokenValid(token, voterId)) {

                System.out.println(
                        "JWT Filter: Token invalid"
                );

                filterChain.doFilter(request, response);
                return;
            }

            System.out.println(
                    "JWT Filter: Voter ID = " + voterId
            );

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                Voter voter = voterRepository
                        .findByVoterId(voterId)
                        .orElse(null);

                if (voter == null) {

                    System.out.println(
                            "JWT Filter: Voter not found"
                    );

                    filterChain.doFilter(request, response);
                    return;
                }

                System.out.println(
                        "JWT Filter: Voter status = "
                                + voter.getStatus()
                );

                if (!"VERIFIED".equals(voter.getStatus())) {

                    System.out.println(
                            "JWT Filter: Voter not verified"
                    );

                    filterChain.doFilter(request, response);
                    return;
                }

                String role = voter.getRole();

                System.out.println(
                        "JWT Filter: Voter role = " + role
                );

                var authorities = List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                );

                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                voterId,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                System.out.println(
                        "JWT Filter: Authentication SUCCESS"
                );
            }

        } catch (JwtException e) {

            System.out.println(
                    "JWT Filter: JWT ERROR = "
                            + e.getMessage()
            );

            SecurityContextHolder.clearContext();

        } catch (Exception e) {

            System.out.println(
                    "JWT Filter: ERROR = "
                            + e.getClass().getName()
                            + " : "
                            + e.getMessage()
            );

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
