package com.example.wx.config;

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

        registry.addResourceHandler("/images/banner/**")
                .addResourceLocations("file:D:/wordProjectFile/admin/banner/");
    }
} 