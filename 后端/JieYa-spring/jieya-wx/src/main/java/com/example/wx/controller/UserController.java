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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    @Value("${upload.avatarPath}")
    private String avatarPath;
    
    @Value("${host.url}")
    private String hostUrl;

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

    /**
     * 修改用户信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result updateUserInfo(
            String nickName,
            MultipartFile avatar,
            Boolean sex,
            String birthday,
            String school,
            String personIntruduction,
            HttpServletRequest request) {
        
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter("token");
        }
        
        if (StringUtils.isEmpty(token)) {
            return Result.error("未登录，请先登录");
        }
        
        // 验证token
        if (!JwtUtil.validateToken(token)) {
            return Result.error("token无效");
        }
        
        String userId = JwtUtil.getUsernameFromToken(token);
        if (StringUtils.isEmpty(userId)) {
            return Result.error("token解析失败");
        }
        TokenUserInfoDto tokenUserInfo = redisComponent.getTokenInfo(token);
        if (tokenUserInfo == null) {
            return Result.error("登录已过期，请重新登录");
        }
        if(!tokenUserInfo.getUserId().equals(userId)){
            return Result.error("修改错误");
        }
        // 创建更新对象，只包含需要修改的字段
        User updateUser = new User();
        updateUser.setUserId(userId);
        
        boolean hasUpdates = false;
        
        if (!StringUtils.isEmpty(nickName)) {
            updateUser.setNickName(nickName);
            tokenUserInfo.setNickName(nickName); // 同时更新Redis中的信息
            hasUpdates = true;
        }
        
        if (sex != null) {
            updateUser.setSex(sex);
            hasUpdates = true;
        }

        //更新头像
        if (avatar != null && !avatar.isEmpty()) {
            try {
                // 生成唯一文件名
                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                // 确保目录存在
                File dir = new File(avatarPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // 保存文件
                File dest = new File(avatarPath + fileName);
                avatar.transferTo(dest);
                updateUser.setAvatar(fileName);
                // 更新Redis中的头像信息，这里存储的是可访问的URL，而不是物理路径
                tokenUserInfo.setAvatar(hostUrl + "/images/avatar/" + fileName);
                hasUpdates = true;
            } catch (Exception e) {
                return Result.error("头像上传失败：" + e.getMessage());
            }
        }

        if (!StringUtils.isEmpty(birthday)) {
            updateUser.setBirthday(birthday);
            hasUpdates = true;
        }
        
        if (!StringUtils.isEmpty(school)) {
            updateUser.setSchool(school);
            hasUpdates = true;
        }
        
        if (!StringUtils.isEmpty(personIntruduction)) {
            updateUser.setPersonIntruduction(personIntruduction);
            hasUpdates = true;
        }
        
        if (!hasUpdates) {
            return Result.error("没有提供任何更新信息");
        }
        
        try {
            boolean success = userService.update(updateUser);
            if (success) {
                // 更新Redis中的用户信息
                redisComponent.saveTokenInfo(token, tokenUserInfo);
                
                return Result.success(tokenUserInfo);
            } else {
                return Result.error("用户信息更新失败");
            }
        } catch (Exception e) {
            return Result.error("用户信息更新失败：" + e.getMessage());
        }
    }
}
