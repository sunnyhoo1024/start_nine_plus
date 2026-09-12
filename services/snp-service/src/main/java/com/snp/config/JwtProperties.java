package com.snp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "snp.jwt")
public class JwtProperties {
    private String secret = "change-me-snp-jwt-secret-key-32b";
    private long expireHours = 72;
}
