// 响应式邮件发送器模块声明
module reactive_email_sender
{
    // Spring 相关依赖
    requires spring.core;
    requires spring.boot;
    requires spring.context;
    requires spring.boot.autoconfigure;
    requires spring.beans;

    // Reactor 响应式编程
    requires transitive reactor.core;

    // Jakarta 依赖
    requires jakarta.annotation;
    requires jakarta.validation;
    requires jakarta.mail;
    requires jakarta.activation;

    // Lombok（编译时依赖）
    requires static lombok;
    requires static org.jetbrains.annotations;

    // 日志
    requires transitive org.slf4j;

    // 导出公共 API 包
    exports io.github.jessez332623.reactive_email_sender.dto;
    exports io.github.jessez332623.reactive_email_sender.autoconfigure;
    exports io.github.jessez332623.reactive_email_sender.utils;
    exports io.github.jessez332623.reactive_email_sender.authorization;
    exports io.github.jessez332623.reactive_email_sender.exception;
    exports io.github.jessez332623.reactive_email_sender;

    // 开放包给 Spring 反射
    opens io.github.jessez332623.reactive_email_sender.autoconfigure
        to spring.core, spring.context;
    opens io.github.jessez332623.reactive_email_sender.impl
        to spring.beans, spring.context;
    opens io.github.jessez332623.reactive_email_sender.authorization
        to spring.core;
    opens io.github.jessez332623.reactive_email_sender.dto
        to spring.core;
}