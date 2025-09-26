package io.github.jessez332623.reactive_email_sender.impl;


import io.github.jessez332623.reactive_email_sender.ReactiveEmailSender;
import io.github.jessez332623.reactive_email_sender.exception.EmailException;
import io.github.jessez332623.reactive_email_sender.dto.EmailContent;
import io.github.jessez332623.reactive_email_sender.utils.EmailFormatVerifier;
import io.github.jessez332623.reactive_email_sender.utils.MimeTypeGetter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;

import static io.github.jessez332623.reactive_email_sender.exception.EmailException.ErrorType.*;
import static java.lang.String.format;

/** 响应式邮件发送器默认实现。*/
@Data
@Slf4j
public class DefaultReactiveEmailSenderImpl implements ReactiveEmailSender
{
    /** 提供 SMTP 服务的运营商主机名（例：smtp.gmail.com、smtp.qq.com）*/
    private final String smtpHost;

    /** SMTP 端口号。*/
    private final int smtpPort;

    /** 最大邮件发送尝试次数 */
    private final int maxAttemptTimes;

    /** 附件大小的上限（单位：MB）*/
    private final int maxAttachmentSize;

    /** 发件人邮箱地址 */
    private final String enterPriceEmailAddress;

    /** 邮箱服务授权码 */
    private final String serviceAuthCode;

    /** 邮件配置属性 */
    private final Properties mailProperties;

    @Contract(" -> new")
    public static @NotNull EmailSenderBuilder
    builder() { return new EmailSenderBuilder(); }

    /**
     * 邮件发送器构造函数，在调用 EmailSenderBuilder::build() 时调用，
     * 外部不可以直接调用。
     *
     * @param builder 邮件发送器实例生成器
     */
    private DefaultReactiveEmailSenderImpl(@NotNull EmailSenderBuilder builder)
    {
        this.smtpHost               = builder.getSmtpHost();
        this.smtpPort               = builder.getSmtpPort();
        this.maxAttemptTimes        = builder.getMaxAttemptTimes();
        this.maxAttachmentSize      = builder.getMaxAttachmentSize();
        this.enterPriceEmailAddress = builder.getEnterPriceEmailAddress();
        this.serviceAuthCode        = builder.getServiceAuthCode();
        this.mailProperties         = builder.getMailProperties();
    }

    /**
     * <p>邮件发送器实例生成器。</p>
     *
     * <span>
     *     在 createEmailSender() 工厂方法中用到了该生成器，
     *     因此需要 @Component 注解标记为一个组件，使得 Spring 能识别到它。
     * </span>
     */
    @Data
    @NoArgsConstructor(access  = AccessLevel.PUBLIC)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EmailSenderBuilder
    {
        private String     smtpHost;
        private int        smtpPort;
        private int        maxAttemptTimes;
        private int        maxAttachmentSize;
        private String     enterPriceEmailAddress;
        private String     serviceAuthCode;
        private Properties mailProperties = new Properties();

        public EmailSenderBuilder
        smtpHost(String host) {
            this.smtpHost = host; return this;
        }
        public EmailSenderBuilder
        smtpPort(int port) {
            this.smtpPort = port; return this;
        }

        public EmailSenderBuilder
        maxAttemptTimes(int attemptTimes)
        {
            this.maxAttemptTimes = attemptTimes;
            return this;
        }

        public EmailSenderBuilder
        maxAttachmentSize(int attachmentSize)
        {
            this.maxAttachmentSize = attachmentSize;
            return this;
        }

        public EmailSenderBuilder
        enterPriceEmailAddress(String address)
        {
            this.enterPriceEmailAddress = address;
            return this;
        }

        public EmailSenderBuilder
        serviceAuthCode(String authCode)
        {
            this.serviceAuthCode = authCode;
            return this;
        }

        /** 添加单条邮件服务配置属性。*/
        public EmailSenderBuilder
        addProperty(String key, String value)
        {
            mailProperties.put(key, value);
            return this;
        }

        /** 添加多条邮件服务配置属性。 */
        public EmailSenderBuilder
        addProperties(@NotNull Map<String, String> sessionProps)
        {
            mailProperties.putAll(sessionProps);
            return this;
        }

        /** 配置默认的邮件属性。*/
        public EmailSenderBuilder setDefaultSessionProperties()
        {
            this.mailProperties.put("mail.smtp.auth", "true");
            this.mailProperties.put("mail.smtp.host", this.smtpHost);
            this.mailProperties.put("mail.smtp.port", this.smtpPort);
            this.mailProperties.put("mail.smtp.connectionpool", "true");
            this.mailProperties.put("mail.smtp.connectionpooltimeout", "5000");
            this.mailProperties.put("mail.smtp.connectionpoolsize", "10");

            this.mailProperties.put("mail.smtp.connectiontimeout", "10000");
            this.mailProperties.put("mail.smtp.timeout", "10000");
            this.mailProperties.put("mail.smtp.writetimeout", "10000");

            switch (this.smtpPort)
            {
                case 465:
                    this.mailProperties.put("mail.smtp.ssl.enable", "true");
                    this.mailProperties.put("mail.smtp.ssl.checkserveridentity", "true");
                    break;

                case 587:
                    this.mailProperties.put("mail.smtp.starttls.enable", "true");
                    this.mailProperties.put("mail.smtp.starttls.required", "true");
                    break;

                case 25:
                    // 端口 25 通常不加密或使用 STARTTLS
                    this.mailProperties.put("mail.smtp.starttls.enable", "true");
                    break;

                default:
                   log.warn(
                       "Uncommon SMTP port: {}, security settings may need adjustment",
                       this.smtpPort
                   );

                   this.mailProperties.put("mail.smtp.starttls.enable", "true");
            }

            return this;
        }

        /** 字段设置完毕，构造出实例并返回。*/
        public DefaultReactiveEmailSenderImpl build()
        {
            return new
            DefaultReactiveEmailSenderImpl(this);
        }
    }

    /**
     * 创建邮件发送会话的静态方法。
     *
     * @param props     邮件配置属性
     * @param userName  邮件发送人邮箱
     * @param password  邮箱服务授权码
     *
     * @return 构造好的 {@link Session} 实例
     */
    @Contract("_, _, _ -> new")
    private static @NotNull Session
    createSession(
        Properties props,
        String userName, String password)
    {
        return
        Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
    }

    /**
     * 检查在发送邮件过程中所抛出的异常，
     * 是否有重发邮件的必要？
     */
    private boolean
    isRetryableError(Throwable throwable)
    {
        if (throwable instanceof EmailException exception)
        {
            // 只有因为网络问题导致的发送失败，才需要进行重试。
            return
            exception.getErrorType()
                     .equals(EmailException.ErrorType.NETWORK_ISSUE);
        }
        
        return false;
    }

    /** 构建邮件正文的数据。*/
    private @NotNull Multipart
    getMultipart(@NotNull EmailContent content)
        throws MessagingException, IOException, NullPointerException
    {
        Multipart multipart = new MimeMultipart();
        BodyPart  textPart  = new MimeBodyPart();

        textPart.setText(content.getTextBody());
        multipart.addBodyPart(textPart);

        // 若 content 内的附件路径不为空，则需要添加附件
        if (content.hasAttachment())
        {
            ByteArrayDataSource attachment
                = getAttachment(
                    Objects.requireNonNull(content.getAttachmentName()),
                    Objects.requireNonNull(content.getAttachmentData())
                );

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(attachment));
            attachmentPart.setFileName(
                MimeUtility.encodeText(
                    content.getAttachmentName(),
                    "utf-8", null
                )
            );

            multipart.addBodyPart(attachmentPart);
        }

        return multipart;
    }

    /** 按照提供的附件名和附件数据构建附件。*/
    private @NotNull ByteArrayDataSource
    getAttachment(
        @NotNull String name, byte @NotNull [] data)
    {

        // 检查附件的大小有没有超过最大值
        if (data.length > this.maxAttachmentSize * 1024 * 1024)
        {
            throw new
            EmailException(
                INVALID_CONTENT,
                "Attachment too large! (MAX_ATTACHMENT_SIZE = " +
                this.maxAttachmentSize + " MB)"
            );
        }

        return new
        ByteArrayDataSource(
            data,
            MimeTypeGetter.getMimeTypeFromExtension(name)
        );
    }

    /**
     * 邮件发送的主要逻辑，由于传统的邮件发送是阻塞式的，
     * 所有我需要调用 {@link Mono#fromCallable(Callable)} 把整个邮件组装发送的逻辑封装，
     * 最后调用 {@link Mono#subscribeOn(Scheduler)} 将整个任务提交给线程池去执行。
     *
     * @param content 邮件内容
     * @param fromName 发件人
     * @param authCode 邮箱服务授权码
     *
     * @return 不发布任何数据的 Mono，表示操作成功完成
     */
    private @NotNull Mono<Void>
    sendEmailReactive(EmailContent content, String fromName, String authCode)
    {
        return Mono.fromCallable(() -> {
            try
            {
                Session newSession
                    = createSession(this.mailProperties, fromName, authCode);

                Message message
                    = new MimeMessage(newSession);

                if (fromName != null) {
                    message.setFrom(new InternetAddress(fromName));
                }

                message.setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(content.getTo())
                );

                message.setSubject(content.getSubject());

                if (!content.hasAttachment()) {
                    message.setText(content.getTextBody());
                }
                else {
                    message.setContent(getMultipart(content));
                }

                Transport.send(message);

                return null;
            }
            catch (AuthenticationFailedException exception)
            {
                throw new EmailException(
                    AUTH_FAILURE,
                    "SMTP auth failed!", exception
                );
            }
            catch (MessagingException exception)
            {
                throw new EmailException(
                    NETWORK_ISSUE,
                    "Net work issue!", exception
                );
            }
            catch (IOException exception)
            {
                throw new EmailException(
                    ATTACHMENT_NOT_EXIST,
                    "Attachment error!", exception
                );
            }

        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    /**
     * 外部可调用的发送邮件的方法。
     *
     * @param emailContent 邮件内容
     *
     * @throws EmailException 当发送邮件失败时抛出
     *
     * @return 表示操作是否正确完成的 {@link Mono}
     */
    @Override
    public Mono<Void>
    sendEmail(@NotNull EmailContent emailContent)
    {
        /*
         * 对于邮件发送过程中因为网络波动而出现的失败，
         * 有比固定时间重试（fixedDelay()）更好的策略，即指数退避。
         *
         * 比如代码中的调用：
         *
         * Retry.backoff(MAX_ATTEMPT_TIMES, Duration.ofSeconds(1))
         *      .maxBackoff(Duration.ofSeconds(10))
         *
         * 表明每失败一次，等待重试的时间就在原有的基础上乘以 2，
         * 具体如下表所示：
         *
         * ----------------------------------
         * 重试次数     等待重试时间（单位：秒）
         *    0             1
         *    1             2
         *    2             4
         *    3             8
         *    4             10
         *    5             10
         * -----------------------------------
         *
         * maxBackoff() 则给重试时间封了顶，
         * 不论重试多少次，等待时间都不会超过 10 秒。
         */
        final Retry retryStrategy
            = Retry.backoff(this.maxAttemptTimes, Duration.ofSeconds(1))
                   .maxBackoff(Duration.ofSeconds(10))
                   .filter(this::isRetryableError)
                   .doBeforeRetry(retrySignal -> {
                        // 记录尝试次数和失败原因
                        log.warn(
                            "Retry attempt {} for email to {}.",
                            retrySignal.totalRetries() + 1,
                            emailContent.getTo()
                        );
                   });

        return
        EmailFormatVerifier
            .isValid(emailContent.getTo())
            .then(
                this.sendEmailReactive(
                    emailContent,
                        this.enterPriceEmailAddress, this.serviceAuthCode)
                    .timeout(Duration.ofSeconds(30L))
                    .retryWhen(retryStrategy)
                    .onErrorResume(exception -> {
                        final String errorMessage
                            = format(
                                "Send email to %s finally failed! max attempt times = %d. Caused by: %s",
                                emailContent.getTo(), this.maxAttemptTimes,
                                exception.getMessage()
                            );

                        return
                        Mono.error(
                            new EmailException(
                                    EmailException.ErrorType.NETWORK_ISSUE,
                                    errorMessage, exception
                            )
                        );
                    })
            );
    }
}