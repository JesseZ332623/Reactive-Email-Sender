package io.github.jessez332623.reactive_email_sender.utils;

import io.github.jessez332623.reactive_email_sender.exception.EmailException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.String.format;

/** 邮箱格式验证工具类。*/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final public class EmailFormatVerifier
{
    /** 通用邮箱正则表达式。*/
    private static final
    Pattern EMAIL_PATTERN
        = Pattern.compile("^[a-zA-Z0-9._%+-]+@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    /** 验证一个邮箱是否符合标准的邮箱格式 */
    public static @NotNull Mono<Void>
    isValid(@NotNull String email)
    {
        Objects.requireNonNull(email, "Param of email not be null!");

        return Mono.fromSupplier(() ->
                EMAIL_PATTERN.matcher(email).matches())
            .filter((isValid) -> isValid)
            .switchIfEmpty(
                Mono.error(
                    new EmailException(
                            EmailException.ErrorType.INVALID_CONTENT,
                            format("%s is invalid email format!", email)
                        )
                    )
                )
            .then();
    }
}
