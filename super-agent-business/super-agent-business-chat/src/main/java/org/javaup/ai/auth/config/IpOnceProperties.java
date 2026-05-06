package org.javaup.ai.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 陌生 IP 一次性使用限制配置。
 */
@Data
@ConfigurationProperties(prefix = "app.chat.ip-once")
public class IpOnceProperties {

    /**
     * 是否开启陌生 IP 仅可使用一次的限制。
     */
    private Boolean enabled = Boolean.FALSE;

    /**
     * Redis key 过期时间（秒）。
     * <p>
     * 为避免永久占用存储，默认给一个较长的过期时间。
     */
    private long ttlSeconds = 60L * 60 * 24 * 365;

    /**
     * 被拒绝时返回给前端的提示语。
     */
    private String message = "陌生ip仅仅只能使用一次";

    /**
     * Redis key 前缀，便于统一清理和隔离。
     */
    private String keyPrefix = "super-agent:ip-once:";
}

