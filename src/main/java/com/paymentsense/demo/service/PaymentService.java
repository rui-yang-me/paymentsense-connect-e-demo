package com.paymentsense.demo.service;

import com.paymentsense.connecte.PaymentsenseClient;
import com.paymentsense.connecte.exception.PaymentsenseException;
import com.paymentsense.connecte.config.Environment;
import com.paymentsense.connecte.model.enums.CurrencyCode;
import com.paymentsense.connecte.model.enums.TransactionType;
import com.paymentsense.connecte.model.PaymentTokenResponse;
import com.paymentsense.connecte.model.PaymentInfo;
import com.paymentsense.connecte.model.CrossReferencePaymentResponse;
import com.paymentsense.connecte.model.CrossReferencePaymentRequest;
import com.paymentsense.connecte.model.ResumePaymentResponse;
import com.paymentsense.connecte.model.PaymentMethodsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.paymentsense.connecte.model.PaymentToken;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * Payment Service - 封装 Paymentsense SDK 的调用
 *
 * @author Paymentsense Demo Team
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${paymentsense.api.key}")
    private String apiKey;

    @Value("${paymentsense.environment}")
    private String environment;

    @Value("${paymentsense.merchant.url}")
    private String merchantUrl;

    private PaymentsenseClient client;

    /**
     * 初始化 Paymentsense 客户端
     */
    @PostConstruct
    public void init() {
        Environment env = "TEST".equalsIgnoreCase(environment) ? Environment.TEST : Environment.PRODUCTION;

        client = PaymentsenseClient.builder()
                .apiKey(apiKey)
                .environment(env)
                .timeout(Duration.ofSeconds(60))
                .build();

        logger.info("Paymentsense Client initialized with environment: {}", environment);
    }

    /**
     * 创建支付 Token
     *
     * @param amount          金额（最小单位，例如：100 = 1.00 GBP）
     * @param currency        货币代码
     * @param orderId         订单ID
     * @param transactionType 交易类型（SALE 或 PREAUTH）
     * @return PaymentTokenResponse
     * @throws PaymentsenseException
     */
    public PaymentTokenResponse createPaymentToken(
            String amount,
            CurrencyCode currency,
            String orderId,
            TransactionType transactionType) throws PaymentsenseException {

        logger.info("Creating payment token for order: {}, amount: {}, currency: {}",
                orderId, amount, currency);
        PaymentToken token = PaymentToken.builder()
                .amount(amount)
                .currency(currency)
                .transactionType(transactionType)
                .orderId(orderId)
                .merchantUrl(merchantUrl)
                .build();

        PaymentTokenResponse response = client.createPaymentToken(token);

        logger.info("Payment token created successfully: {}", response.getId());
        return response;
    }

    /**
     * 获取支付信息
     *
     * @param token 支付 Token
     * @return PaymentInfo
     * @throws PaymentsenseException
     */
    public PaymentInfo getPaymentInfo(String token) throws PaymentsenseException {
        logger.info("Getting payment info for token: {}", token);

        PaymentInfo paymentInfo = client.getPaymentInfo(token);

        logger.info("Payment info retrieved - Status Code: {}, Success: {}",
                paymentInfo.getStatusCode(), paymentInfo.isSuccess());

        return paymentInfo;
    }

    /**
     * 创建退款 Token
     *
     * @param originalToken 原支付 Token ID
     * @param amount          退款金额（最小单位，例如：100 = 1.00 GBP）
     * @param currency        货币代码
     * @param orderId         订单ID
     * @return PaymentTokenResponse
     * @throws PaymentsenseException
     */
    public PaymentTokenResponse createRefundToken(
            String originalToken,
            String amount,
            CurrencyCode currency,
            String orderId) throws PaymentsenseException {

        logger.info("Creating refund token for original token: {}, amount: {}, currency: {}",
                originalToken, amount, currency);

        // 获取原支付信息以验证
        PaymentInfo originalPayment = client.getPaymentInfo(originalToken);

        if (!originalPayment.isSuccess()) {
            throw new PaymentsenseException(
                    "Cannot create refund - original payment was not successful. Status: " +
                            originalPayment.getStatusCode());
        }

        // 创建退款类型的 Token
        PaymentToken token = PaymentToken.builder()
                .amount(amount)
                .currency(currency)
                .transactionType(TransactionType.REFUND)
                .orderId(orderId)
                .merchantUrl(merchantUrl)
                .crossReference(originalPayment.getCrossReference())
                .build();

        PaymentTokenResponse response = client.createPaymentToken(token);

        logger.info("Refund token created successfully: {}", response.getId());
        return response;
    }

    /**
     * 执行 Cross Reference Payment（用于退款、VOID等操作）
     *
     * @param refundToken  退款 Token
     * @param crossReference 原支付的 Cross Reference
     * @return CrossReferencePaymentResponse
     * @throws PaymentsenseException
     */
    public CrossReferencePaymentResponse executeCrossReferencePayment(
            String refundToken,
            String crossReference) throws PaymentsenseException {

        logger.info("Executing cross reference payment for token: {}, crossReference: {}",
                refundToken, crossReference);

        if (crossReference == null || crossReference.isEmpty()) {
            throw new PaymentsenseException("Cross reference cannot be empty");
        }

        // 构建 Cross Reference Payment 请求
        CrossReferencePaymentRequest request = CrossReferencePaymentRequest.builder()
                .crossReference(crossReference)
                .build();

        // 执行 Cross Reference Payment
        CrossReferencePaymentResponse response = client.executeCrossReferencePayment(
                refundToken,
                request);

        logger.info("Cross reference payment completed - Status Code: {}", response.getStatusCode());

        return response;
    }

    /**
     * 恢复支付会话
     *
     * @param token 支付 Token
     * @return ResumePaymentResponse
     * @throws PaymentsenseException
     */
    public ResumePaymentResponse resumePayment(String token) throws PaymentsenseException {
        logger.info("Resuming payment for token: {}", token);

        ResumePaymentResponse response = client.resumePayment(token);

        logger.info("Payment resumed successfully: {}", response);

        return response;
    }

    /**
     * 撤销访问令牌
     *
     * @param token Token
     * @return RevokeTokenResponse
     * @throws PaymentsenseException
     */
    public void revokeAccessToken(String token) throws PaymentsenseException {
        logger.info("Revoking access token: {}", token);
        client.revokeAccessToken(token);
    }

    /**
     * 获取客户的支付方式列表
     *
     * @return PaymentMethodsResponse
     * @throws PaymentsenseException
     */
    public PaymentMethodsResponse getPaymentMethods() throws PaymentsenseException {
        logger.info("Getting payment methods for customer");

        PaymentMethodsResponse response = client.getPaymentMethods();

        logger.info("Retrieved payment methods: {}",response);

        return response;
    }
}
