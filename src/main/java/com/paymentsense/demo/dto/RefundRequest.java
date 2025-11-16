package com.paymentsense.demo.dto;

/**
 * 退款请求
 */
public class RefundRequest {
    private String token;  // 原支付的 Token
    private String amount;   // 退款金额（必填）
    private String currency; // 货币代码（必填，例如：GBP）

    public RefundRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokenId) {
        this.token= tokenId;
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

    @Override
    public String toString() {
        return "RefundRequest{" +
                "tokenId='" + token + '\'' +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
