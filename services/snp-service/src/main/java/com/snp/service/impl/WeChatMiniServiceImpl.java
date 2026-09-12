package com.snp.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.snp.common.exception.BizException;
import com.snp.config.WeChatProperties;
import com.snp.service.WeChatMiniService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatMiniServiceImpl implements WeChatMiniService {

    private final WeChatProperties weChatProperties;

    @Override
    public Map<String, String> code2session(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException("code不能为空");
        }
        if (weChatProperties.isMock()) {
            String openid = "mock_" + DigestUtil.md5Hex(code).substring(0, 16);
            Map<String, String> result = new HashMap<>();
            result.put("openid", openid);
            result.put("session_key", "mock_session");
            result.put("unionid", "");
            return result;
        }
        if (!StringUtils.hasText(weChatProperties.getAppId()) || !StringUtils.hasText(weChatProperties.getSecret())) {
            throw new BizException("微信配置未完成");
        }
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                weChatProperties.getAppId(), weChatProperties.getSecret(), code);
        String body = HttpUtil.get(url, 5000);
        JSONObject json = JSONUtil.parseObj(body);
        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            log.warn("WeChat code2session failed: {}", body);
            throw new BizException("微信登录失败: " + json.getStr("errmsg"));
        }
        Map<String, String> result = new HashMap<>();
        result.put("openid", json.getStr("openid"));
        result.put("session_key", json.getStr("session_key"));
        result.put("unionid", json.getStr("unionid", ""));
        return result;
    }
}
