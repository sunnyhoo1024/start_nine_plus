package com.snp.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtUtil {

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(String secret, long expireHours) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expireMillis = expireHours * 3600_000L;
    }

    public String createToken(String subject, Long userId, String type, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }
        claims.put("userId", userId);
        claims.put("type", type);
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    public String createAdminToken(Long userId, String username, List<String> permissions) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("permissions", permissions);
        return createToken(username, userId, "admin", extra);
    }

    public String createAppToken(Long userId, String openid) {
        return createToken(openid, userId, "app", null);
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        Object v = claims.get("userId");
        if (v instanceof Number n) {
            return n.longValue();
        }
        return v == null ? null : Long.parseLong(v.toString());
    }

    public String getType(Claims claims) {
        Object v = claims.get("type");
        return v == null ? null : v.toString();
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissions(Claims claims) {
        Object v = claims.get("permissions");
        if (v instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }
}
