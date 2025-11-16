package com.paymentsense.demo.controller;

import com.paymentsense.connecte.model.CrossReferencePaymentResponse;
import com.paymentsense.connecte.model.PaymentInfo;
import com.paymentsense.connecte.model.PaymentTokenResponse;
import com.paymentsense.connecte.model.ResumePaymentResponse;
import com.paymentsense.connecte.model.PaymentMethodsResponse;
import com.paymentsense.connecte.model.enums.CurrencyCode;
import com.paymentsense.connecte.model.enums.TransactionType;
import com.paymentsense.demo.dto.PaymentRequest;
import com.paymentsense.demo.dto.PaymentStatusResponse;
import com.paymentsense.demo.dto.RefundRequest;
import com.paymentsense.demo.dto.TokenResponse;
import com.paymentsense.demo.service.PaymentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Controller - 处理支付相关的 HTTP 请求
 *
 * @author Paymentsense Demo Team
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Value("${paymentsense.web.url}")
    private String webUrl;

    /**
     * 创建支付 Token
     * POST /api/create-token
     */
    @PostMapping("/create-token")
    public ResponseEntity<?> createToken(@RequestBody PaymentRequest request) {
        try {
            logger.info("Received create token request: {}", request);

            // 生成唯一订单ID
            String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // 解析货币代码
            CurrencyCode currency = CurrencyCode.valueOf(request.getCurrency().toUpperCase());

            // 解析交易类型
            TransactionType transactionType = "PREAUTH".equalsIgnoreCase(request.getTransactionType())
                    ? TransactionType.PREAUTH
                    : TransactionType.SALE;

            // 创建支付 Token
            PaymentTokenResponse tokenResponse = paymentService.createPaymentToken(
                    request.getAmount(),
                    currency,
                    orderId,
                    transactionType);

            // 构建响应
            TokenResponse response = new TokenResponse();
            response.setSuccess(true);
            response.setTokenId(tokenResponse.getId());
            response.setOrderId(orderId);
            response.setAmount(request.getAmount());
            response.setCurrency(request.getCurrency());
            response.setWebUrl(webUrl);

            logger.info("Token created successfully: {}", tokenResponse.getId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request parameter", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", "Invalid parameter: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Failed to create token", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 查询支付状态
     * GET /api/payment-status/{token}
     */
    @GetMapping("/payment-status/{token}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String token) {
        try {
            logger.info("Checking payment status for token: {}", token);

            PaymentInfo paymentInfo = paymentService.getPaymentInfo(token);

            PaymentStatusResponse response = new PaymentStatusResponse();
            response.setSuccess(paymentInfo.isSuccess());
            response.setStatusCode(paymentInfo.getStatusCode().getCode());
            response.setMessage(paymentInfo.getMessage());
            response.setAuthCode(paymentInfo.getAuthCode());
            response.setCrossReference(paymentInfo.getCrossReference());
            response.setCardType(paymentInfo.getCardType());
            response.setCardNumber(paymentInfo.getCardNumber());
            response.setTransactionDateTime(paymentInfo.getTransactionDateTime());
            response.setCardName(paymentInfo.getCardName());
            response.setExpiryDate(paymentInfo.getExpiryDate());

            logger.info("Payment status: {}", paymentInfo.isSuccess() ? "SUCCESS" : "FAILED");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get payment status", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 创建退款 Token
     * POST /api/create-refund-token
     */
    @PostMapping("/create-refund-token")
    public ResponseEntity<?> createRefundToken(@RequestBody RefundRequest request) {
        try {
            logger.info("Received create refund token request for original token: {}", request.getToken());

            // 生成唯一订单ID
            String orderId = "REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // 解析货币代码
            CurrencyCode currency = CurrencyCode.valueOf(request.getCurrency().toUpperCase());

            // 创建退款 Token
            PaymentTokenResponse tokenResponse = paymentService.createRefundToken(
                    request.getToken(),
                    request.getAmount(),
                    currency,
                    orderId);

            // 构建响应
            TokenResponse response = new TokenResponse();
            response.setSuccess(true);
            response.setTokenId(tokenResponse.getId());
            response.setOrderId(orderId);
            response.setAmount(request.getAmount());
            response.setCurrency(request.getCurrency());
            response.setWebUrl(webUrl);

            logger.info("Refund token created successfully: {}", tokenResponse.getId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request parameter", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", "Invalid parameter: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Failed to create refund token", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 执行 Cross Reference Payment（用于退款、VOID等）
     * POST /api/cross-reference-payments/{tokenId}
     */
    @PostMapping("/cross-reference-payments/{tokenId}")
    public ResponseEntity<?> executeCrossReferencePayment(
            @PathVariable String tokenId,
            @RequestBody Map<String, String> request) {
        try {
            logger.info("Received cross reference payment request for token: {}", tokenId);

            String crossReference = request.get("crossReference");
            if (crossReference == null || crossReference.isEmpty()) {
                Map<String, Object> error = new HashMap<>(2);
                error.put("success", false);
                error.put("error", "crossReference is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            CrossReferencePaymentResponse response = paymentService.executeCrossReferencePayment(
                    tokenId,
                    crossReference);

            Map<String, Object> result = new HashMap<>(8);
            result.put("success", response.isSuccess());
            result.put("statusCode", response.getStatusCode());
            result.put("message", response.getMessage());
            result.put("authCode", response.getAuthCode());

            logger.info("Cross reference payment completed with status code: {}", response.getStatusCode());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Failed to execute cross reference payment", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 恢复支付
     * POST /api/payments/{paymentId}/resume
     */
    @PostMapping("/payments/{token}/resume")
    public ResponseEntity<?> resumePayment(@PathVariable String token) {
        try {
            logger.info("Received resume payment request for payment: {}", token);

            ResumePaymentResponse resumeResponse = paymentService.resumePayment(token);

            Map<String, Object> response = new HashMap<>(8);
            response.put("success", resumeResponse.isSuccess());
            response.put("statusCode", resumeResponse.getStatusCode());
            response.put("message", resumeResponse.getMessage());
            response.put("authCode", resumeResponse.getAuthCode());

            logger.info("Payment resumed successfully: {}", resumeResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to resume payment", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 撤销访问令牌
     * DELETE /api/access-tokens/{tokenId}
     */
    @DeleteMapping("/access-tokens/{token}")
    public ResponseEntity<?> revokeAccessToken(@PathVariable String token) {
        try {
            logger.info("Received revoke token request for token: {}", token);

            paymentService.revokeAccessToken(token);

            Map<String, Object> response = new HashMap<>(4);
            response.put("success", true);
            logger.info("Access token revoked successfully: {}", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to revoke access token", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 获取支付方式列表
     * GET /api/payment-methods?customerId={customerId}
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getPaymentMethods() {
        try {
            logger.info("Received get payment methods request");

            PaymentMethodsResponse methodsResponse = paymentService.getPaymentMethods();

            Map<String, Object> response = new HashMap<>(4);
            response.put("success", true);
            response.put("paymentMethods", methodsResponse);

            logger.info("Retrieved payment methods: {}", response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get payment methods", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * 健康检查
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>(2);
        response.put("status", "OK");
        response.put("service", "Paymentsense SDK Demo");
        return ResponseEntity.ok(response);
    }
}
