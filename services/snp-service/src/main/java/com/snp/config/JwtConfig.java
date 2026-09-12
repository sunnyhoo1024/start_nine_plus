package com.snp.config;

import com.snp.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtProperties.getSecret(), jwtProperties.getExpireHours());
    }
}
