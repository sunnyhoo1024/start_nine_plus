package com.snp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "snp.wechat")
public class WeChatProperties {
    private String appId = "";
    private String secret = "";
    private boolean mock = true;
}
