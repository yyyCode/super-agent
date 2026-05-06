package org.javaup.ai.auth.config;

import org.javaup.ai.auth.support.AdminAuthInterceptor;
import org.javaup.ai.auth.support.IpOnceInterceptor;
import org.javaup.ai.auth.support.PreviewModeInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 后台管理登录与预览模式的 MVC 配置。
 */
@Configuration
@EnableConfigurationProperties({AdminAuthProperties.class, PreviewModeProperties.class, IpOnceProperties.class})
public class AdminWebMvcConfiguration implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    private final PreviewModeInterceptor previewModeInterceptor;

    private final IpOnceInterceptor ipOnceInterceptor;

    public AdminWebMvcConfiguration(AdminAuthInterceptor adminAuthInterceptor,
                                    PreviewModeInterceptor previewModeInterceptor,
                                    IpOnceInterceptor ipOnceInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.previewModeInterceptor = previewModeInterceptor;
        this.ipOnceInterceptor = ipOnceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
            .addPathPatterns("/manage/**", "/admin/auth/me");

        registry.addInterceptor(ipOnceInterceptor)
            .addPathPatterns("/api/chat/stream");

        registry.addInterceptor(previewModeInterceptor)
            .addPathPatterns("/**");
    }
}
