package org.zzt.note.commons.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 配置。
 */
@Configuration
@Slf4j
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 当前跨域请求最大有效时长（1 天）。
     */
    private static final long MAX_AGE = 24 * 60 * 60;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("register global cors mappings for app-note");
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(MAX_AGE);
    }
}
