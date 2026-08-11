package com.hotelerp.frontoffice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class JwtTokenExtractor {

    private final ObjectMapper objectMapper;

    public JwtTokenExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LoginUser extractLoginUser(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            log.warn("Invalid JWT token format");
            return null;
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            JsonNode claims = objectMapper.readTree(payloadJson);

            Long userId = getLongValue(claims, "userId");
            if (userId == null && claims.hasNonNull("sub")) {
                try {
                    userId = Long.parseLong(claims.get("sub").asText());
                } catch (NumberFormatException ignored) {
                }
            }

            String userName = getTextValue(claims, "userName");
            String username = getTextValue(claims, "username");
            if (userName == null && username != null) {
                userName = username;
            } else if (username == null && userName != null) {
                username = userName;
            }

            Long hotelId = getLongValue(claims, "hotelId");
            String hotelName = getTextValue(claims, "hotelName");
            String email = getTextValue(claims, "email");
            String tokenId = getTextValue(claims, "jti");

            List<String> authorities = new ArrayList<>();
            if (claims.has("authorities") && claims.get("authorities").isArray()) {
                claims.get("authorities").forEach(node -> authorities.add(node.asText()));
            }

            return LoginUser.builder()
                    .userId(userId)
                    .userName(userName)
                    .username(username)
                    .hotelId(hotelId)
                    .hotelName(hotelName)
                    .email(email)
                    .authorities(authorities)
                    .tokenId(tokenId)
                    .build();
        } catch (Exception ex) {
            log.error("Failed to parse JWT token payload", ex);
            return null;
        }
    }

    private Long getLongValue(JsonNode node, String fieldName) {
        if (node.hasNonNull(fieldName)) {
            JsonNode val = node.get(fieldName);
            if (val.isNumber()) {
                return val.longValue();
            } else if (val.isTextual()) {
                try {
                    return Long.parseLong(val.asText());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    private String getTextValue(JsonNode node, String fieldName) {
        if (node.hasNonNull(fieldName)) {
            return node.get(fieldName).asText();
        }
        return null;
    }
}
