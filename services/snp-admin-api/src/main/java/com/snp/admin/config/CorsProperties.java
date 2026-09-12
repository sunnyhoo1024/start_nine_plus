package com.snp.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "snp.cors")
public class CorsProperties {
    private String allowedOrigins = "*";
}
