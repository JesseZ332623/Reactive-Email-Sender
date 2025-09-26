package io.github.jessez332623.reactive_email_sender.authorization;

/**
 * 邮箱服务授权码获取接口，
 * 用户可以实现该接口，从任何地方（如：环境变量、数据库等）获取授权码。
 */
public interface EmailServiceAuthCodeGetter
{
    default String get() { return null; }
}
