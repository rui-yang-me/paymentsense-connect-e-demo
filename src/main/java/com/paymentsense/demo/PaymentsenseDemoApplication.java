package com.paymentsense.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Paymentsense SDK Demo Application
 *
 * 功能：
 * 1. 创建支付 Token
 * 2. 执行支付
 * 3. 查询支付状态
 * 4. 执行退款
 *
 * 启动后访问：http://localhost:8080
 *
 * @author Paymentsense Demo Team
 */
@SpringBootApplication
public class PaymentsenseDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentsenseDemoApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Paymentsense SDK Demo 已启动");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
