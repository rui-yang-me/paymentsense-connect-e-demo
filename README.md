# Paymentsense SDK Demo

基于 Paymentsense Connect-E SDK 的 Spring Boot 演示应用。

## 功能

1. ✅ 创建支付 Token
2. ✅ 查询支付状态
3. ✅ 创建退款 Token
4. ✅ 执行 Cross Reference Payment
5. ✅ 恢复支付（Resume Payment）
6. ✅ 撤销访问令牌（Revoke Access Token）
7. ✅ 获取支付方式列表（Payment Methods）

## 技术栈

- Java 17
- Spring Boot 2.7.18
- Paymentsense Connect-E SDK 1.0.3
- Bootstrap 4 (前端UI)
- 全局异常处理（@RestControllerAdvice）

## 快速开始

### 方式 1: 手动构建和运行

#### 步骤 1: 确保 SDK 已构建

```bash
cd ../paymentsense-sdk
mvn clean install -DskipTests
cd ../paymentsense-sdk-demo
```

#### 步骤 2: 运行应用

使用 Maven:
```bash
mvn spring-boot:run
```

或者使用 IDE:
- 直接运行 `PaymentsenseDemoApplication.java`

#### 步骤 3: 访问应用

打开浏览器访问: http://localhost:8080

## 依赖说明

### 重要：SDK 依赖配置

```xml
<dependency>
    <groupId>io.github.rui-yang-me</groupId>
    <artifactId>connect-e-sdk</artifactId>
    <version>1.0.3</version>
</dependency>
```

### SDK 依赖的传递依赖

SDK 使用以下库（已在 pom.xml 中配置）：
- Jackson (JSON 序列化)
- Lombok (代码简化)

## 测试卡信息

- **卡号**: 4111111111111111
- **过期日期**: 12/25 (任意未来日期)
- **CVV**: 111

## 故障排除

### 问题 1: 找不到 SDK 类

**错误**: `cannot find symbol: class PaymentsenseClient`

**解决方案**:
```bash
cd ../paymentsense-sdk
mvn clean install -DskipTests
```

### 问题 2: SDK 依赖下载失败

**错误**: 依赖下载不下来，可能是 Maven 索引还没建立

**解决方案**: 手动执行以下命令下载依赖
```bash
mvn dependency:get -DrepoUrl=https://repo1.maven.org/maven2 -Dartifact=io.github.rui-yang-me:paymentsense-connect-e-sdk:1.0.3 -U
```

### 问题 3: Maven 未安装

如果系统没有 Maven，可以：
1. 使用 IDE 的内置 Maven
2. 下载 Maven Wrapper: `mvn wrapper:wrapper`
3. 安装 Maven: [https://maven.apache.org/install.html](https://maven.apache.org/install.html)

### 问题 4: 端口被占用

修改 `application.properties`:
```properties
server.port=8081
```

## API 端点

### 基础支付功能
- `POST /api/create-token` - 创建支付 Token
- `GET /api/payment-status/{tokenId}` - 查询支付状态
- `POST /api/create-refund-token` - 创建退款 Token
- `POST /api/cross-reference-payments/{tokenId}` - 执行 Cross Reference Payment

### 新增功能 🆕
- `POST /api/payments/{paymentId}/resume` - 恢复支付会话
- `DELETE /api/access-tokens/{tokenId}` - 撤销访问令牌
- `GET /api/payment-methods` - 获取客户的支付方式列表

### 系统功能
- `GET /api/health` - 健康检查

详细的 API 使用示例请参考 [NEW_API_EXAMPLES.md](NEW_API_EXAMPLES.md)。

## 异常处理

应用使用全局异常处理器（`@RestControllerAdvice`）统一处理所有异常：

### 特性
- ✅ 统一的错误响应格式
- ✅ 自动异常类型到 HTTP 状态码映射
- ✅ 开发环境显示详细错误，生产环境隐藏敏感信息
- ✅ 集中式日志记录
- ✅ 减少控制器代码量

### 错误响应格式

```json
{
  "success": false,
  "error": "错误消息",
  "errorCode": "ERROR_CODE",
  "statusCode": 400,
  "path": "/api/endpoint",
  "timestamp": "2024-01-01T12:00:00",
  "details": "详细信息（仅开发环境）"
}
```

### 异常映射

| 异常类型 | HTTP 状态码 | 错误代码 |
|---------|-----------|---------|
| ValidationException | 400 | VALIDATION_ERROR |
| IllegalArgumentException | 400 | INVALID_PARAMETER |
| AuthenticationException | 401 | AUTHENTICATION_ERROR |
| PaymentsenseException | 500 | PAYMENT_ERROR |
