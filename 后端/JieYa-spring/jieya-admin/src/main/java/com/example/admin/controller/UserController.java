package com.example.admin.controller;


import com.example.admin.service.MessageService;
import com.example.admin.service.UserService;
import com.example.common.Enum.MessageTypeEnum;
import com.example.common.common.Result;
import com.example.common.pojo.Message;
import com.example.common.pojo.User;
import com.example.common.redis.RedisComponent;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

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
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/list")
    public Result list(@RequestParam Integer pageNum, @RequestParam Integer pageSize,@RequestParam String status,@RequestParam String username,@RequestParam String userId) {
        userService.getlist(pageNum, pageSize,status,username,userId);
        return Result.success(userService.getlist(pageNum, pageSize,status,username,userId));
    }

    @RequestMapping("/forbidUser")
    public Result forbidUser(@RequestParam String userId) {
        userService.forbidUser(userId);
        return Result.success("用户已禁止登录");
    }

    @RequestMapping("/unforbidUser")
    public Result unforbidUser(@RequestParam String userId) {
        userService.unforbidUser(userId);
        return Result.success("用户已解除禁止登录");
    }

    @RequestMapping("/sendMessage2User")
    public Result sendMessage2User(@NotEmpty String userId, @NotEmpty String content) {
        Message message = new Message();
        message.setUser(userId);
        message.setContent(content);
        message.setType("SYSTEM-USER");
        message.setTime(new Date());
        messageService.save( message);
        redisComponent.setMessage(message);
        return Result.success("消息已发送");
    }

    @RequestMapping("/getMessage2UserType")
    public Result getMessage2UserType() {
        return Result.success(messageService.getMessage2UserType());
    }

    @RequestMapping("/sendMessage2AllUser")
    public Result sendMessage2AllUser(@NotEmpty String content) {
        String type = "SYSTEM-USER";
        Message message = new Message();
        message.setUser("all");
        message.setContent(content);
        message.setType(type);
        message.setTime(new Date());
        messageService.save( message);
        redisComponent.setMessage(message);
        return Result.success("消息已发送");
    }

    @RequestMapping("/deleteUser")
    public Result deleteUser(@RequestParam String userId) {
        userService.deleteUser(userId);
        return Result.success("用户已删除");
    }

    @RequestMapping("/addUser")
    public Result addUser(@RequestBody User user) {
        userService.addUser(user);
        return Result.success("用户已添加");
    }

    @RequestMapping("/updateUser")
    public Result updateUser(@RequestBody User user) {
        userService.updateById(user);
        return Result.success("用户已更新");
    }

    
    
    
}

