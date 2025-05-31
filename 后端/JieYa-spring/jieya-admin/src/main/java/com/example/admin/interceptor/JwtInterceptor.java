package com.example.admin.interceptor;

import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 放行登录接口
        if (request.getRequestURI().contains("/admin/login")) {
            return true;
        }
        if (request.getRequestURI().contains("/checkcode")) {
            return true;
        }

        // 从cookies中获取token
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("adminToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("未登录，请先登录");
        }

        // 验证token是否有效
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("token无效");
        }

        String username = JwtUtil.getUsernameFromToken(token);
        if(!username.equals("admin")){
            throw new RuntimeException("账号不存在");
        }
//        String redisToken = redisComponent.getAdminToken(token);
//
//        if (StringUtils.isEmpty(redisToken) || !token.equals(redisToken)) {
//            throw new RuntimeException("token已过期，请重新登录");
//        }

        return true;
    }
}