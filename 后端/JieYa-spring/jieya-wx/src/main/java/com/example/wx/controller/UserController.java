package com.example.wx.controller;


import com.example.common.WxDto.TokenUserInfoDto;
import com.example.common.common.Address;
import com.example.common.common.ControllerTool;
import com.example.common.common.Result;
import com.example.common.constants.Constants;
import com.example.common.pojo.User;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import com.example.common.utils.StringTools;
import com.example.wx.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private ControllerTool controllerTool;

    @GetMapping("okok")
    public Result<String> okok(){
        return Result.success("okok");
    }

    @GetMapping("name/{nick_name}")
    public Result<List<User>> searchNickName(@PathVariable("nick_name") String nickName) {
        return Result.success(userService.searchByNickName(nickName));
    }

    @RequestMapping("/register")
    public Result register(@RequestParam @NotEmpty @Email String email,
                           @RequestParam @NotEmpty @Size(max = 20) String nickName,
                           @RequestParam @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                           @RequestParam @NotEmpty String checkCodeKey,
                           @RequestParam @NotEmpty String checkCode
    ) {

        if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
            return Result.error("验证码错误");
        }
        redisComponent.cleanCheckCode(checkCodeKey);

        String result = userService.register(email,nickName,password);
        if(result.equals("注册成功"))
            return Result.success(result);

        else
            return Result.error(result);
    }

    @RequestMapping("/login")
    public Result login(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam @NotEmpty @Email String email,
                        @RequestParam @NotEmpty String password,
                        @RequestParam @NotEmpty String checkCodeKey,
                        @RequestParam @NotEmpty String checkCode) {

        if(!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))){
            return Result.error("验证码错误");
        }
        
        // 清验证码
        redisComponent.cleanCheckCode(checkCodeKey);
        
        String ip = Address.getIpAddr();
        TokenUserInfoDto tokenUserInfoDto = userService.login(email, password, ip);

        return Result.success(tokenUserInfoDto);
    }

    /**
     * 自动登录接口
     * 根据token自动登录
     */
    @GetMapping("/autologin")
    public Result autologin(HttpServletRequest request) {
        // 从请求头获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter("token");
        }
        
        if (StringUtils.isEmpty(token)) {
            return Result.error("未找到有效token");
        }
        
        // 验证token是否有效
        if (!JwtUtil.validateToken(token)) {
            return Result.error("token无效");
        }
        
        // 从Redis获取用户信息
        TokenUserInfoDto userInfo = redisComponent.getTokenInfo(token);
        if (userInfo == null) {
            return Result.error("登录已过期，请重新登录");
        }
        
        // 如果token快过期，可以刷新token（可选）
        long currentTime = System.currentTimeMillis();
        if (userInfo.getExpireAt() - currentTime < Constants.REDIS_KEY_EXPIRES_ONE_DAY) {
            // 刷新token的过期时间
            userInfo.setExpireAt(currentTime + Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
            redisComponent.saveTokenInfo(token,userInfo);
        }
        
        return Result.success(userInfo);
    }

    @RequestMapping("/loginout")
    public Result loginout(HttpServletResponse response) {
        controllerTool.cleanCookie(response);
        return Result.success("退出登录成功");
    }

}
