# 完整的 Java Mail Session Properties

| 属性名                                 | 默认值   | 说明              |
|-------------------------------------|-------|-----------------|
| **基本连接配置**                          |       |                 |
| `mail.smtp.host`                    | -     | SMTP服务器主机名      |
| `mail.smtp.port`                    | 25    | SMTP服务器端口       |
| `mail.smtp.user`                    | -     | 用户名(通常不需要)      |
| `mail.smtp.from`                    | -     | 发件人地址           |
| `mail.smtp.localhost`               | 自动检测  | 本地主机名           |
| `mail.smtp.localaddress`            | -     | 绑定到的本地地址        |
| `mail.smtp.localport`               | -     | 绑定到的本地端口        |
| **认证配置**                            |       |                 |
| `mail.smtp.auth`                    | false | 启用身份验证          |
| `mail.smtp.auth.mechanisms`         | -     | 认证机制列表          |
| `mail.smtp.auth.login.disable`      | false | 禁用LOGIN认证       |
| `mail.smtp.auth.plain.disable`      | false | 禁用PLAIN认证       |
| `mail.smtp.auth.digest-md5.disable` | false | 禁用DIGEST-MD5    |
| `mail.smtp.auth.ntlm.disable`       | false | 禁用NTLM认证        |
| **安全配置**                            |       |                 |
| `mail.smtp.ssl.enable`              | false | 启用SSL加密         |
| `mail.smtp.starttls.enable`         | false | 启用STARTTLS      |
| `mail.smtp.starttls.required`       | false | 要求STARTTLS      |
| `mail.smtp.ssl.checkserveridentity` | false | 检查服务器身份         |
| `mail.smtp.ssl.trust`               | -     | 信任的SSL证书        |
| `mail.smtp.ssl.socketFactory`       | -     | 自定义SSL socket工厂 |
| `mail.smtp.ssl.socketFactory.port`  | 465   | SSL socket工厂端口  |
| `mail.smtp.ssl.protocols`           | -     | 启用SSL协议版本       |
| `mail.smtp.ssl.ciphersuites`        | -     | 启用SSL密码套件       |
| **超时配置**                            |       |                 |
| `mail.smtp.timeout`                 | 无穷    | socket读写超时(ms)  |
| `mail.smtp.writetimeout`            | 无穷    | socket写超时(ms)   |
| `mail.smtp.connectiontimeout`       | 无穷    | 连接建立超时(ms)      |
| **连接池配置**                           |       |                 |
| `mail.smtp.connectionpool`          | false | 启用连接池           |
| `mail.smtp.connectionpooltimeout`   | -     | 连接池超时时间         |
| `mail.smtp.connectionpoolsize`      | -     | 连接池大小           |
| **调试和日志**                           |       |                 |
| `mail.debug`                        | false | 启用调试输出          |
| `mail.smtp.debug`                   | false | SMTP特定调试        |
| `mail.smtp.reportsuccess`           | false | 报告成功命令          |
| **其他配置**                            |       |                 |
| `mail.smtp.allow8bitmime`           | false | 允许8bit MIME     |
| `mail.smtp.sendpartial`             | false | 允许部分发送          |
| `mail.smtp.sasl.enable`             | false | 启用SASL认证        |
| `mail.smtp.sasl.mechanisms`         | -     | SASL机制          |
| `mail.smtp.sasl.authorizationid`    | -     | SASL授权ID        |
| `mail.smtp.sasl.realm`              | -     | SASL域           |
| `mail.smtp.quitwait`                | true  | 退出时等待响应         |
| `mail.smtp.dsn.notify`              | -     | 投递状态通知          |
| `mail.smtp.dsn.ret`                 | -     | DSN返回选项         |

