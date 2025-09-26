# 基于 javax.mail 且无缝集成响应式编程的邮件发送器

## 用法

### 依赖地址

本依赖已发布至 Maven 中央仓库，
可以访问：[Reactive-Email-Sender](https://central.sonatype.com/artifact/io.github.jessez332623/reactive_email_sender)，
也可以在 pom.xml 中直接配置：

```XML
<dependency>
    <groupId>io.github.jessez332623</groupId>
    <artifactId>reactive_email_sender</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 属性配置

```properties
# 是否启用本依赖？（默认启用）
app.reactive-email-sender.enabled=true

# 提供 SMTP 服务的运营商主机名
app.reactive-email-sender.smtpHost=smtp.qq.com

# SMTP 端口号
app.reactive-email-sender.smtpPort=465

# 最大邮件发送尝试次数（默认 3 回）
app.reactive-email-sender.max-attempt-times=5

# 附件大小的上限（单位：MB，默认为 8）
app.reactive-email-sender.max-attachment-size=8

# 发件人邮箱地址
app.reactive-email-sender.sender-email=[your-email]

# 邮箱服务授权码（不建议直接写配置上）
# app.reactive-email-sender.auth-code=[your-auth-code]

# 额外的 Session 属性添加
# app.reactive-email-sender.session-props=[props]
```

### 从配置之外读取邮箱授权码

本依赖声明 `EmailServiceAuthCodeGetter` 接口：

```java
/**
 * 邮箱服务授权码获取接口，
 * 用户可以实现该接口，从任何地方（如：环境变量、数据库等）获取授权码。
 */
public interface EmailServiceAuthCodeGetter
{
    default String get() { return null; }
}
```

可以实现该接口，示例如下（从数据库读取授权码）：

```java
@Service
public class EmailAuthService implements EmailServiceAuthCodeGetter
{
    @Autowired
    private EmailAuthRepository emailAuthRepository;

    @Value("${app.reactive-email-sender.sender-email}")
    private String senderEmail;

    @Override
    public String get()
    {
        return
        this.emailAuthRepository
            .findAuthCodeByEmail(senderEmail)
            .block();
    }
}
```

## 代码速览

- [响应式邮件发送器默认实现](https://github.com/JesseZ332623/Reactive-Email-Sender/blob/main/src/main/java/io/github/jessez332623/reactive_email_sender/impl/DefaultReactiveEmailSenderImpl.java)

- [向指定用户发送邮件的内容实体](https://github.com/JesseZ332623/Reactive-Email-Sender/blob/main/src/main/java/io/github/jessez332623/reactive_email_sender/dto/EmailContent.java)

- [附件 Mine Type 获取器](https://github.com/JesseZ332623/Reactive-Email-Sender/blob/main/src/main/java/io/github/jessez332623/reactive_email_sender/utils/MimeTypeGetter.java)

## 文档

- [完整的 Java Mail Session Properties 表](https://github.com/JesseZ332623/Reactive-Email-Sender/blob/main/documents/%E5%AE%8C%E6%95%B4%E7%9A%84%20Java%20Mail%20Session%20Properties%20%E8%A1%A8.md)

- [测试-01 发送 3 封预设格式的邮件](https://github.com/JesseZ332623/Reactive-Email-Sender/blob/main/documents/%E6%B5%8B%E8%AF%95-01%20%E5%8F%91%E9%80%81%203%20%E5%B0%81%E9%A2%84%E8%AE%BE%E6%A0%BC%E5%BC%8F%E7%9A%84%E9%82%AE%E4%BB%B6.md)

## LICENCE

[Apache License Version 2.0](https://github.com/JesseZ332623/Reactive-Email-Sender/blob/main/LICENCE)

## Latest Update

*2025.09.26*