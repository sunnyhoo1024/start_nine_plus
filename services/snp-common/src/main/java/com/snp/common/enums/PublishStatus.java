package com.snp.common.enums;

import lombok.Getter;

@Getter
public enum PublishStatus {
    DRAFT(0),
    ONLINE(1);

    private final int code;

    PublishStatus(int code) {
        this.code = code;
    }
}
