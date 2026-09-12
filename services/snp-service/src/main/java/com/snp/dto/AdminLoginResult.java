package com.snp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminLoginResult {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private List<String> roles;
    private List<String> permissions;
}
