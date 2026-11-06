package com.example.task_flow.security;

import java.io.Serializable;
import java.util.Date;

public class JwtDetails implements Serializable {
    private final String jti;
    private final Date expiration;

    public JwtDetails(String jti, Date expiration) {
        this.jti = jti;
        this.expiration = expiration;
    }

    public String getJti() { return jti; }
    public Date getExpiration() { return expiration; }
}