package com.hotelerp.frontoffice.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@Component
@RequestScope
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUser {

    private Long userId;
    private String userName;
    private String username;
    private Long hotelId;
    private String hotelName;
    private String email;
    private List<String> authorities;
    private String tokenId;

    public void setValuesFrom(LoginUser other) {
        if (other == null) {
            return;
        }
        this.userId = other.userId;
        this.userName = other.userName;
        this.username = other.username;
        this.hotelId = other.hotelId;
        this.hotelName = other.hotelName;
        this.email = other.email;
        this.authorities = other.authorities;
        this.tokenId = other.tokenId;
    }
}
