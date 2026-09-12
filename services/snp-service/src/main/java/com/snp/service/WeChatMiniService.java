package com.snp.service;

import java.util.Map;

public interface WeChatMiniService {
    Map<String, String> code2session(String code);
}
