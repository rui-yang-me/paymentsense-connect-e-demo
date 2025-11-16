package com.paymentsense.demo.aspect;

import com.paymentsense.connecte.exception.ApiException;
import com.paymentsense.connecte.exception.AuthenticationException;
import com.paymentsense.connecte.exception.PaymentsenseException;
import com.paymentsense.connecte.exception.ValidationException;
import com.paymentsense.demo.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * 全局异常处理器
 * <p>
 * 统一处理 PaymentController 及其他控制器的异常，
 * 返回标准化的错误响应格式。
 * </p>
 *
 * @author Paymentsense Demo Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    /**
     * 处理验证异常
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {

        logger.warn("Validation error: {}", ex.getMessage());

        String path = getRequestPath(request);
        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                path
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理认证异常
     * HTTP 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        logger.error("Authentication error: {}", ex.getMessage());

        String path = getRequestPath(request);
        ErrorResponse errorResponse = ErrorResponse.of(
                "Authentication failed. Please check your API credentials.",
                "AUTHENTICATION_ERROR",
                HttpStatus.UNAUTHORIZED.value(),
                path
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 处理 API 异常
     * HTTP 状态码根据 ApiException 中的状态码决定
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException ex, WebRequest request) {

        logger.error("API error (status {}): {}", ex.getStatusCode(), ex.getMessage());

        String path = getRequestPath(request);
        HttpStatus httpStatus = HttpStatus.resolve(ex.getStatusCode());
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getMessage(),
                ex.getErrorCode() != null ? ex.getErrorCode() : "API_ERROR",
                httpStatus.value(),
                path
        );

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    /**
     * 处理 Paymentsense 通用异常
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(PaymentsenseException.class)
    public ResponseEntity<ErrorResponse> handlePaymentsenseException(
            PaymentsenseException ex, WebRequest request) {

        logger.error("Paymentsense error: {}", ex.getMessage(), ex);

        String path = getRequestPath(request);
        ErrorResponse errorResponse;

        if (isDevelopmentMode()) {
            // 开发环境返回详细错误信息
            errorResponse = ErrorResponse.withDetails(
                    "Payment processing failed: " + ex.getMessage(),
                    ex.getErrorCode() != null ? ex.getErrorCode() : "PAYMENT_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path,
                    getStackTraceAsString(ex)
            );
        } else {
            // 生产环境返回简化错误信息
            errorResponse = ErrorResponse.of(
                    "Payment processing failed. Please try again later.",
                    ex.getErrorCode() != null ? ex.getErrorCode() : "PAYMENT_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path
            );
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理非法参数异常
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        logger.warn("Invalid argument: {}", ex.getMessage());

        String path = getRequestPath(request);
        ErrorResponse errorResponse = ErrorResponse.of(
                "Invalid parameter: " + ex.getMessage(),
                "INVALID_PARAMETER",
                HttpStatus.BAD_REQUEST.value(),
                path
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理空指针异常
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex, WebRequest request) {

        logger.error("Null pointer exception occurred", ex);

        String path = getRequestPath(request);
        ErrorResponse errorResponse;

        if (isDevelopmentMode()) {
            errorResponse = ErrorResponse.withDetails(
                    "An unexpected error occurred: Null value encountered",
                    "NULL_POINTER_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path,
                    getStackTraceAsString(ex)
            );
        } else {
            errorResponse = ErrorResponse.of(
                    "An unexpected error occurred. Please contact support.",
                    "INTERNAL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path
            );
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理所有未捕获的异常
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        String path = getRequestPath(request);
        ErrorResponse errorResponse;

        if (isDevelopmentMode()) {
            // 开发环境返回详细错误
            errorResponse = ErrorResponse.withDetails(
                    "An unexpected error occurred: " + ex.getMessage(),
                    "INTERNAL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path,
                    getStackTraceAsString(ex)
            );
        } else {
            // 生产环境返回通用错误消息
            errorResponse = ErrorResponse.of(
                    "An unexpected error occurred. Please try again later.",
                    "INTERNAL_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    path
            );
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 获取请求路径
     */
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return request.getDescription(false);
    }

    /**
     * 判断是否为开发模式
     */
    private boolean isDevelopmentMode() {
        return "dev".equalsIgnoreCase(activeProfile) ||
                "development".equalsIgnoreCase(activeProfile) ||
                "local".equalsIgnoreCase(activeProfile);
    }

    /**
     * 获取异常堆栈信息
     */
    private String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName())
                .append(": ")
                .append(throwable.getMessage())
                .append("\n");

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            // 只显示前 10 层堆栈，避免响应过大
            if (sb.length() > 2000) {
                sb.append("\t... (truncated)\n");
                break;
            }
        }

        return sb.toString();
    }
}
