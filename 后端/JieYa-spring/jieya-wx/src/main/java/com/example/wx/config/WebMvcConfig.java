package com.example.wx.config;

import com.example.wx.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
        import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/autologin")
                .excludePathPatterns("/checkcode")
                .excludePathPatterns("/ws")
                .excludePathPatterns("/message/**")
                .excludePathPatterns("/images/**");  // 排除所有静态资源
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/temp/**")
                .addResourceLocations("file:D:/wordProjectFile/temp/");

        registry.addResourceHandler("/images/banner/**")
                .addResourceLocations("file:D:/wordProjectFile/admin/banner/");

        registry.addResourceHandler("/images/avatar/**")
                .addResourceLocations("file:D:/wordProjectFile/user/avatar/");
        registry.addResourceHandler("/images/venue/**")
                .addResourceLocations("file:D:/wordProjectFile/admin/venue/");
        registry.addResourceHandler("/images/prop/**")
                .addResourceLocations("file:D:/wordProjectFile/admin/prop/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8081", "http://localhost:8082")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
} 