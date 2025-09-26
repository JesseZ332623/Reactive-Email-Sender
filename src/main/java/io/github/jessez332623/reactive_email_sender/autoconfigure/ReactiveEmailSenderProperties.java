package io.github.jessez332623.reactive_email_sender.autoconfigure;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/** 响应式邮件发送器依赖自动装配属性类。*/
@Data
@ToString
@ConfigurationProperties(prefix = "app.reactive-email-sender")
public class ReactiveEmailSenderProperties
{
    /** 是否启用本依赖？（默认启用）*/
    private boolean enabled = true;

    /** 提供 SMTP 服务的运营商主机名（例：smtp.gmail.com、smtp.qq.com）*/
    @NotBlank(message = "SMTP host not be null")
    private String smtpHost;

    /**
     * SMTP 端口号，不同的服务商对邮件开放的标准服务端口都不同，具体如下所示：
     * <pre>
     *     Gmail   587 STARTTLS
     *     Outlook 587 STARTTLS
     *     Yahoo   465 SSL
     *     QQ-Mail 465 or 587 SSL
     * </pre>
     * 至于其他服务商的端口号可以查询他们提供的文档。
     */
    @Min(value = 1,     message = "SMTP port not less then 1")
    @Max(value = 65535, message = "SMTP port not less then 65535")
    private int smtpPort;

    /** 附件大小的上限（单位：MB，默认为 8）*/
    @Positive(message = "Max attachment size must be positive")
    private int maxAttachmentSize = 8;

    /** 发件人邮箱地址 */
    @Email(message = "Sender email format invalid")
    private String senderEmail;

    /** 邮箱服务授权码 */
    private String authCode;

    /** 指数退避策略相关属性。*/
    private BackOff backoff = new BackOff();

    /**
     * 邮件会话属性配置，示例如下：
     * <ul>
     *     <li>app.reactive-email-sender.session-props.mail.smtp.connectionpoolsize=10</li>
     *     <li>app.reactive-email-sender.session-props.mail.smtp.connectionpooltimeout=5000</li>
     * </ul>
     */
    private Map<String, String> sessionProps = new HashMap<>();

    @Data
    @NoArgsConstructor
    public static class BackOff
    {
        /** 最大邮件发送尝试次数（默认 3 回）*/
        @Positive(message = "Max attempt times must be positive")
        private int maxAttemptTimes = 3;

        /** 指数退避起始时间间隔（单位：秒，默认为 1）*/
        @Positive(message = "Start backoff interval must be positive")
        private long startBackoffInterval = 1;

        /** 指数退避封顶时间间隔（单位：秒，默认为 10）*/
        @Positive(message = "Max backoff interval must be positive")
        private long maxBackoffInterval = 10;
    }
}