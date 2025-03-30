package com.example.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于映射本地文件路径到URL
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/temp/**")
                .addResourceLocations("file:D:/wordProjectFile/temp/");

        registry.addResourceHandler("/images/official/**")
                .addResourceLocations("file:D:/wordProjectFile/admin/banner/");

        registry.addResourceHandler("/images/venue/**")
                .addResourceLocations("file:D:/wordProjectFile/admin/venue/");

        registry.addResourceHandler("/images/prop/**")
                .addResourceLocations("file:D:/wordProjectFile/admin/prop/");

        registry.addResourceHandler("/images/avatar/**")
                .addResourceLocations("file:D:/wordProjectFile/user/avatar/");

        registry.addResourceHandler("/message/**")
                .addResourceLocations("file:D:/wordProjectFile/message/");
    }
} 