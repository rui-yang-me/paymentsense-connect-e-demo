package com.paymentsense.demo.dto;

/**
 * 创建支付 Token 请求
 */
public class PaymentRequest {
    private String amount;          // 金额（最小单位）
    private String currency;        // 货币代码（GBP, EUR, USD）
    private String transactionType; // 交易类型（SALE, PREAUTH）

    public PaymentRequest() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", transactionType='" + transactionType + '\'' +
                '}';
    }
}
