package io.github.jessez332623.reactive_email_sender.dto;

import io.github.jessez332623.reactive_email_sender.utils.VerifyCodeGenerator;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

import static java.lang.String.format;

/**
 * <p>向指定用户发送邮件的内容实体，有 </p>
 *
 * <ul>
 *     <li>{@link EmailContent#fromVarify(String, String, int, Duration)}</li>
 *     <li>{@link EmailContent#fromJustText(String, String, String)}</li>
 *     <li>{@link EmailContent#formWithAttachment(String, String, String, String, String, byte[])}</li>
 * </ul>
 *
 * <p>三个预设构造，也支持生成器模式进行自定义。</p>
 */
@Data
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access  = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailContent
{
    /** 发给谁 (如 PerterGriffen@gmail.com) */
    @Getter
    private String to;

    /** 邮件主题 */
    private String subject;

    /** 邮件正文 */
    private String textBody;

    /** 附件文件名（可以为 null 表示没有附件）*/
    @Nullable
    private String attachmentName;

    /** 附件数据（可以为 null 表示没有附件）*/
    private byte @Nullable [] attachmentData;

    /**
     * 检查这封邮件是否包含附件。
     *
     * <ul>
     *     <li>true  包含附件</li>
     *     <li>false 不包含附件</li>
     * </ul>
     */
    public boolean hasAttachment()
    {
        return
        Objects.nonNull(this.attachmentName) &&
        Objects.nonNull(this.attachmentData) &&
        this.attachmentData.length > 0;
    }

    /**
     * 发送验证码邮件需要的内容。
     *
     * @param userName   收件人姓名
     * @param userEmail  收件人邮箱
     * @param digits     验证码位数
     * @param expired    验证码有效期（一般从属性中获取）
     *
     * @return 发布验证码邮件内容的 Mono
     */
    public static @NotNull Mono<EmailContent>
    fromVarify(
        String userName, String userEmail,
        int digits, @NotNull Duration expired
    )
    {
        return
        VerifyCodeGenerator.generateVerifyCode(digits)
            .map((varifyCode) -> {
                EmailContent emailContent = new EmailContent();

                emailContent.setTo(userEmail);
                emailContent.setSubject("用户：" + userName + " 请查收您的验证码。");
                emailContent.setTextBody(
                    format(
                        "用户：%s 您的验证码是：[%s]，" +
                            "请在 %s 分钟内完成验证，超过 %s 分钟后验证码自动失效！",
                        userName, varifyCode,
                        expired.toMinutes(), expired.toMinutes()
                    )
                );

                // 验证码邮件不需要附件内容
                emailContent.setAttachmentName(null);
                emailContent.setAttachmentData(null);

                return emailContent;
            });
    }

    /**
     * 发送纯文本邮件的内容。
     *
     * @param userEmail     收件人邮箱
     * @param subject       邮件标题
     * @param message       邮件正文
     *
     * @return 发布纯文本邮件的 {@link Mono}
     */
    public static @NotNull Mono<EmailContent>
    fromJustText(String userEmail, String subject, String message)
    {
        return
        Mono.fromCallable(() -> {
            EmailContent emailContent = new EmailContent();

            emailContent.setTo(userEmail);
            emailContent.setSubject(subject);
            emailContent.setTextBody(message);

            // 纯文本邮件没有附件内容
            emailContent.setAttachmentName(null);
            emailContent.setAttachmentData(null);

            return emailContent;
        });
    }

    /**
     * 发送带附件的邮件所需要的内容。
     *
     * @param userName       收件人姓名
     * @param userEmail      收件人邮箱
     * @param subject        邮件标题（按 “用户：XXX” 开头）
     * @param message        邮件正文
     * @param attachmentName 附件文件名
     * @param attachmentData 附件完整数据
     *
     * @return 发布带附件的邮件内容的 Mono
     */
    public static @NotNull Mono<EmailContent>
    formWithAttachment(
        String userName, String userEmail,
        String subject, String message,
        String attachmentName, byte[] attachmentData)
    {
        return
        Mono.fromCallable(() -> {
            EmailContent emailContent = new EmailContent();

            emailContent.setTo(userEmail);
            emailContent.setSubject("用户：" + userName + " " + subject);
            emailContent.setTextBody(message);
            emailContent.setAttachmentName(attachmentName);
            emailContent.setAttachmentData(attachmentData);

            return emailContent;
        });
    }
}