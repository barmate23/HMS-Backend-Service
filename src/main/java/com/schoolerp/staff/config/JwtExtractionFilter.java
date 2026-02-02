package com.schoolerp.staff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class JwtExtractionFilter implements Filter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String payload = token.split("\\.")[1];

                String json = new String(Base64.getUrlDecoder().decode(payload));
                Map<String, Object> claims = mapper.readValue(json, Map.class);

                String userEmail = (String) claims.get("sub");
                UserContext.setUser(userEmail);  // 🔥 Store user globally for request
            }

            chain.doFilter(request, response);

        } finally {
            UserContext.clear(); // important to avoid thread leaks
        }
    }
}
