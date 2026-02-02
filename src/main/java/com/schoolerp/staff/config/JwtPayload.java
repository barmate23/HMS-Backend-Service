package com.schoolerp.staff.config;

import lombok.Data;

@Data
public class JwtPayload {
    private String sub;
    private long iat;
    private long exp;
}
