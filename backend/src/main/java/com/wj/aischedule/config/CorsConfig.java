package com.wj.aischedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 对所有接口都允许跨域
                registry.addMapping("/**")
                        .allowedOriginPatterns("*") // 允许所有本地开发端口
                        .allowedMethods("GET","POST","PUT","DELETE","OPTIONS") // 允许方法
                        .allowCredentials(true) // 是否允许携带 Cookie
                        .allowedHeaders("*") // 允许所有请求头
                        .maxAge(3600); // 预检请求缓存时间
            }
        };
    }
}
