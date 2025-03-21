package com.example.common.common;

import com.example.common.constants.Constants;
import com.example.common.WxDto.TokenUserInfoDto;
import com.example.common.redis.RedisComponent;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ControllerTool {

    @Resource
    private RedisComponent redisComponent;

    public  void saveToken2Cookie(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(Constants.TOKEN_WEB, token);
        cookie.setMaxAge(Constants.REDIS_KEY_EXPIRES_ONE_DAY*7 / 1000);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public  void saveTokenAdminCookie(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(Constants.TOKEN_ADMIN, token);
        cookie.setMaxAge(Constants.REDIS_KEY_EXPIRES_ONE_DAY*7 / 1000);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public TokenUserInfoDto getTokenUserInfoDto() {
    // 从请求头获取token
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String token = request.getHeader("Authorization");
    
    // 如果请求头中没有，尝试从请求参数获取
    if (StringUtils.isEmpty(token)) {
        token = request.getParameter("token");
    }
    
    if (StringUtils.isEmpty(token)) {
        return null;
    }
    
    return redisComponent.getTokenInfo(token);
}

    public void cleanCookie(HttpServletResponse response){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie[] cookies = request.getCookies();
        String token = null;
        for(Cookie cookie:cookies){
            if(cookie.getName().equals((Constants.TOKEN_ADMIN)))
            {
                redisComponent.cleanToken(cookie.getValue());
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                break;
            }

        }
    }
}
