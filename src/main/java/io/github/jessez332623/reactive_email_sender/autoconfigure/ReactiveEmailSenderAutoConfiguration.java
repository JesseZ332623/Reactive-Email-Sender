package io.github.jessez332623.reactive_email_sender.autoconfigure;

import io.github.jessez332623.reactive_email_sender.ReactiveEmailSender;
    import io.github.jessez332623.reactive_email_sender.authorization.EmailServiceAuthCodeGetter;
import io.github.jessez332623.reactive_email_sender.exception.EmailException.ErrorType;
import io.github.jessez332623.reactive_email_sender.exception.EmailException;
import io.github.jessez332623.reactive_email_sender.impl.DefaultReactiveEmailSenderImpl;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** 响应式邮件发送器 Spring 自动配置类。*/
@Configuration
@ConditionalOnProperty(
    prefix         = "app.reactive-email-sender",
    name           = "enabled",
    havingValue    = "true",
    matchIfMissing = true // 默认启用本依赖
)
@EnableConfigurationProperties({ReactiveEmailSenderProperties.class})
public class ReactiveEmailSenderAutoConfiguration
{
    @Autowired
    private Validator validator;

    @Autowired
    private EmailServiceAuthCodeGetter authCodeGetter;

    @Autowired
    private ReactiveEmailSenderProperties properties;

    /**
     * 手动执行验证，
     * 如果出现格式问题，收集信息并包装成 {@link EmailException} 异常。
     */
    @PostConstruct
    private void validateConfiguration()
    {
        final
        Set<ConstraintViolation<ReactiveEmailSenderProperties>> violations
            = validator.validate(this.properties);

        if (!violations.isEmpty())
        {
            StringBuilder errorInfo
                = new StringBuilder("Reactive email sender varify failed:\n");

            for (var violation : violations)
            {
                errorInfo.append(
                    String.format(
                        "- %s: %s\n",
                        violation.getPropertyPath(), violation.getMessage()
                    )
                );
            }

            throw new
            EmailException(ErrorType.CONFIG_MISSING, errorInfo.toString());
        }
    }

    /**
     * 获取邮箱授权码
     *（优先级：用户 {@link EmailServiceAuthCodeGetter} 接口实现 -> 用户配置文件）
     *
     * @throws EmailException 若在所有渠道都拿不到授权码时抛出
     */
    private String getAuthCode()
    {
        final String authCode
            = Optional.ofNullable(this.authCodeGetter.get())
                      .orElse(this.properties.getAuthCode());

        if (Objects.isNull(authCode))
        {
            throw new
            EmailException(
                ErrorType.CONFIG_MISSING,
                "Property <auth-code> is missing..."
            );
        }

        return authCode;
    }

    /** 响应式邮件发送器自动装配方法。*/
    @Bean
    @ConditionalOnMissingBean(value = {DefaultReactiveEmailSenderImpl.class})
    public ReactiveEmailSender reactiveEmailSender()
    {
        return
        DefaultReactiveEmailSenderImpl.builder()
            .smtpHost(this.properties.getSmtpHost())
            .smtpPort(this.properties.getSmtpPort())
            .maxAttemptTimes(this.properties.getMaxAttemptTimes())
            .maxAttachmentSize(this.properties.getMaxAttachmentSize())
            .enterPriceEmailAddress(this.properties.getSenderEmail())
            .serviceAuthCode(this.getAuthCode())
            .setDefaultSessionProperties()
            .addProperties(this.properties.getSessionProps())
            .build();
    }
}