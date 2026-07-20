package com.nabarangpur.erp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        System.out.println("==================================");
        System.out.println("Request URI : " + request.getRequestURI());
        System.out.println("Header      : " + header);

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            try {

                System.out.println("Token Valid : " + jwtUtil.isTokenValid(token));
                System.out.println("Refresh     : " + jwtUtil.isRefreshToken(token));

                String username = jwtUtil.extractUsername(token);
                System.out.println("Username    : " + username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                System.out.println("Enabled     : " + userDetails.isEnabled());
                System.out.println("Not Locked  : " + userDetails.isAccountNonLocked());
                System.out.println("Authorities : " + userDetails.getAuthorities());

                if (jwtUtil.isTokenValid(token)
                        && !jwtUtil.isRefreshToken(token)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    

                    System.out.println("Authentication Set Successfully");
                    System.out.println(SecurityContextHolder.getContext().getAuthentication());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SecurityContextHolder.clearContext();
                writeError(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
