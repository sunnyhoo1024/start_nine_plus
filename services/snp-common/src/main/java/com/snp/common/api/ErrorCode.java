package com.snp.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 统一业务错误码。
 * <ul>
 *   <li>200：成功</li>
 *   <li>4xx/5xx：通用 HTTP 语义错误</li>
 *   <li>1xxx：通用业务失败</li>
 *   <li>2xxx：积分 / 活动 / 兑换等细分业务码</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    OK(200, "ok"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    INTERNAL(500, "系统异常"),

    /** 未细分的业务失败 */
    BIZ_ERROR(1000, "业务处理失败"),

    POINTS_INSUFFICIENT(2001, "积分不足"),
    STOCK_INSUFFICIENT(2002, "库存不足"),
    LOTTERY_NO_PRIZE(2003, "暂无可抽奖品"),
    LOTTERY_LIMIT(2004, "抽奖次数已用完"),
    ACTIVITY_UNAVAILABLE(2005, "活动不可用"),
    REDEEM_UNAVAILABLE(2006, "兑换不可用"),
    AUTH_FAILED(2007, "认证失败");

    private final int code;
    private final String message;
}
