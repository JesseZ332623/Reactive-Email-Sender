package io.github.jessez332623.reactive_email_sender;

import io.github.jessez332623.reactive_email_sender.dto.EmailContent;
import io.github.jessez332623.reactive_email_sender.exception.EmailException;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

/** 封装了 javax.mail 库的响应式邮件发送器接口。*/
public interface ReactiveEmailSender
{
    /**
     * 外部可调用的发送邮件的方法。
     *
     * @param emailContent 邮件内容
     *
     * @throws EmailException 当发送邮件失败时抛出
     *
     * @return 表示操作是否正确完成的 {@link Mono}
     */
    Mono<Void>
    sendEmail(@NotNull EmailContent emailContent);
}