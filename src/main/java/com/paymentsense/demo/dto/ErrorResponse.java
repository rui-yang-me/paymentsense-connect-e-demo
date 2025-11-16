package com.paymentsense.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一错误响应 DTO
 *
 * @author Paymentsense Demo Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    /**
     * 错误状态，总是 false
     */
    private boolean success;

    /**
     * 错误消息
     */
    private String error;

    /**
     * 错误代码（可选）
     */
    private String errorCode;

    /**
     * HTTP 状态码
     */
    private int statusCode;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 详细错误信息（开发环境可用）
     */
    private String details;

    /**
     * 创建简单错误响应
     */
    public static ErrorResponse of(String error, int statusCode, String path) {
        return ErrorResponse.builder()
                .success(false)
                .error(error)
                .statusCode(statusCode)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建带错误代码的响应
     */
    public static ErrorResponse of(String error, String errorCode, int statusCode, String path) {
        return ErrorResponse.builder()
                .success(false)
                .error(error)
                .errorCode(errorCode)
                .statusCode(statusCode)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建详细错误响应（用于开发环境）
     */
    public static ErrorResponse withDetails(String error, String errorCode, int statusCode, String path, String details) {
        return ErrorResponse.builder()
                .success(false)
                .error(error)
                .errorCode(errorCode)
                .statusCode(statusCode)
                .path(path)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }
}
