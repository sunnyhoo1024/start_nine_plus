package com.snp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppLoginResult {
    private String token;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Long balance;
}
