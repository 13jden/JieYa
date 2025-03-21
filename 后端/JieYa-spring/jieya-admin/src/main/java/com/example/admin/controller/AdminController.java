package com.example.admin.controller;


import com.example.common.common.ControllerTool;
import com.example.common.common.Result;
import com.example.common.config.AppConfig;
import com.example.common.constants.Constants;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import com.example.common.utils.StringTools;
//import com.example.common.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ControllerTool controllerTool;

//    @Autowired
//    private JwtUtil jwtUtil;

    @RequestMapping("/login")
    public Result login(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam @NotEmpty String account,
                        @RequestParam @NotEmpty String password,
                        @RequestParam @NotEmpty String checkCodeKey,
                        @RequestParam @NotEmpty String checkCode) throws Exception {
        if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
            return Result.error("验证码错误");
        }
        System.out.println("收到");
        // 清除验证码
        redisComponent.cleanCheckCode(checkCodeKey);
        String configuredAccount = appConfig.getAdminAccount();
        String configuredPassword = appConfig.getAdminPassword();
        if (!account.equals(configuredAccount)||!password.equals(configuredPassword)) {
            return Result.error("账号或密码错误");
        }

        String jwtToken = null;
        // 清理上一条登录JWT
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(Constants.TOKEN_ADMIN)) {
                    jwtToken = cookie.getValue();
                }
            }
        }
        if (!StringTools.isEmpty(jwtToken)) {
            if (jwtUtil.validateToken(jwtToken)) {
                redisComponent.cleanAdminToken(jwtToken);
            }
        }
        // 生成新的JWT
        jwtToken = jwtUtil.generateToken(account);
        controllerTool.saveTokenAdminCookie(response, jwtToken);
        return Result.success("登录成功");
    }



}
