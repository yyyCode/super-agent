package org.javaup.ai.auth.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.javaup.ai.auth.config.IpOnceProperties;
import org.javaup.ai.chatagent.support.StreamEventWriter;
import org.javaup.common.ApiResponse;
import org.javaup.handle.RedissonDataHandle;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 陌生 IP 仅允许使用一次的拦截器。
 * <p>
 * 仅拦截流式聊天入口，避免影响后台管理与只读演示等其他链路。
 */
@Component
public class IpOnceInterceptor implements HandlerInterceptor {

    private final IpOnceProperties ipOnceProperties;
    private final RedissonDataHandle redissonDataHandle;
    private final ObjectMapper objectMapper;
    private final StreamEventWriter streamEventWriter;

    public IpOnceInterceptor(IpOnceProperties ipOnceProperties,
                             RedissonDataHandle redissonDataHandle,
                             ObjectMapper objectMapper,
                             StreamEventWriter streamEventWriter) {
        this.ipOnceProperties = ipOnceProperties;
        this.redissonDataHandle = redissonDataHandle;
        this.objectMapper = objectMapper;
        this.streamEventWriter = streamEventWriter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!Boolean.TRUE.equals(ipOnceProperties.getEnabled())) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = resolveClientIp(request);
        if (clientIp == null || clientIp.isBlank()) {
            // 无法可靠识别来源时，不做限制，避免误伤。
            return true;
        }

        String key = ipOnceProperties.getKeyPrefix() + clientIp.trim();
        boolean allowed = redissonDataHandle.trySetIfAbsent(
            key,
            "1",
            Math.max(1, ipOnceProperties.getTtlSeconds()),
            TimeUnit.SECONDS
        );
        if (allowed) {
            return true;
        }

        writeStreamReject(response, ipOnceProperties.getMessage());
        return false;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0];
            if (first != null && !first.isBlank()) {
                return first.trim();
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isBlank()) {
            // Forwarded: for=203.0.113.195;proto=http;by=203.0.113.43
            int forIndex = forwarded.toLowerCase().indexOf("for=");
            if (forIndex >= 0) {
                String tail = forwarded.substring(forIndex + 4).trim();
                int end = tail.indexOf(';');
                String value = (end >= 0 ? tail.substring(0, end) : tail).trim();
                value = value.replace("\"", "");
                if (value.startsWith("[")) {
                    int idx = value.indexOf(']');
                    return idx > 0 ? value.substring(1, idx) : value;
                }
                int colon = value.indexOf(':');
                return colon > 0 ? value.substring(0, colon) : value;
            }
        }
        return request.getRemoteAddr();
    }

    private void writeStreamReject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/event-stream;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.getWriter().write("data: " + streamEventWriter.error(message) + "\n\n");
        response.getWriter().flush();
    }

    @SuppressWarnings("unused")
    private void writeJsonReject(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(message)));
    }
}

