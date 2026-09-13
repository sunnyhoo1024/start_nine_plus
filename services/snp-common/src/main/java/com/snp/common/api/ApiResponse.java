package com.snp.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), data);
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message != null ? message : errorCode.getMessage());
    }

    /**
     * 失败响应保持与成功相同结构：{ code, message, data }。
     * data 中带 errorMsg，便于前端统一读取。
     */
    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> fail(int code, String message) {
        String msg = message == null ? "" : message;
        T payload = (T) Map.of("errorMsg", msg);
        return new ApiResponse<>(code, msg, payload);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return fail(ErrorCode.BIZ_ERROR, message);
    }
}
