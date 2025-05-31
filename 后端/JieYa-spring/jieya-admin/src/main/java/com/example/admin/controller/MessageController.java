package com.example.admin.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.service.MessageService;
import com.example.common.adminDto.AdminMessageDto;
import com.example.common.common.Result;
import com.example.common.pojo.Message;
import com.example.admin.WebSocketService.MessageProducerService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-27
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageProducerService messageProducerService;

    @Autowired
    private MessageService messageService;

    @Value("${host.url}")
    private String hostUrl;

    @PostMapping("/send")
    public Result sendMessage(@NotEmpty String toUser,
                               String content,
                              @NotEmpty String type,
                              String fileUrl) {
        Message message = new Message();
        // 设置发送者
        message.setUser("admin");
        message.setTime(new Date());
        message.setToUser(toUser);
        message.setContent(content);
        if (fileUrl != null && !fileUrl.isEmpty() && !fileUrl.equals("undefined")) {
            // 使用正则表达式匹配日期目录和文件名部分
            Pattern pattern = Pattern.compile(".*/files/message/(\\d+/[^/]+)");
            Matcher matcher = pattern.matcher(fileUrl);
            
            if (matcher.find()) {
                String relativePath = matcher.group(1);
                message.setFileUrl(relativePath);
            } else {
                // 如果没有匹配到预期格式，尝试原来的处理方式
                String prefix = hostUrl + "/files/message/";
                if (fileUrl.startsWith(prefix)) {
                    // 移除前缀，只保存相对路径部分
                    String relativePath = fileUrl.substring(prefix.length());
                    message.setFileUrl(relativePath);
                } else {
                    // 如果没有前缀，直接保存
                    message.setFileUrl(fileUrl);
                }
            }
        }
        message.setType(type);
        // 生产消息
        messageProducerService.produceMessage(message);
        messageService.save(message);
        return Result.success("消息发送成功");
    }
    //获取用户消息/订单消息/系统消息对应的消息数
    @RequestMapping("/getAllList")
    public Result getAllList() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("user", messageService.getUserMessageCount());
        map.put("order", messageService.getOrderMessageCount());
        map.put("system", messageService.getSystemMessageCount());
        return Result.success(map);
    }
    //获取用户发送的消息,的用户列表
    @RequestMapping("/getUserMessageList")
    public Result getUserMessageList(@RequestParam int pageNum, @RequestParam int pageSize) {
        Page<AdminMessageDto> messages = messageService.getMessagesListByToUser(pageNum, pageSize);
        return Result.success(messages);
    }
    //获取用户发送的消息,的用户列表
    @RequestMapping("/getUserItemMessageList")
    public Result getUserItemMessageList(@RequestParam String userId,@RequestParam int pageNum, @RequestParam int pageSize) {
        Page<Message> messages = messageService.getMessagesItemListByToUser(userId, pageNum, pageSize);
        return Result.success(messages);
    }

    //获取用户的预约/完成订单消息
    @RequestMapping("/getOrderMessage")
    public Result getOrderMessage(@RequestParam int pageNum, @RequestParam int pageSize) {
        Page<AdminMessageDto> messages = messageService.getOrderMessage(pageNum, pageSize);
        return Result.success(messages);
    }
    //获取完成系统操作，用户申诉等消息
    @RequestMapping("/getSystemMessage")
    public Result getSystemMessage(@RequestParam int pageNum, @RequestParam int pageSize) {
        Page<AdminMessageDto> messages = messageService.getSystemMessage(pageNum, pageSize);
        return Result.success(messages);
    }
    //删除消息
    @RequestMapping("/deleteMessage")
    public Result deleteMessage(@RequestParam String messageId) {
        messageService.deleteMessage(messageId);
        return Result.success("消息删除成功");
    }
    //批量删除消息
    @RequestMapping("/deleteMessageList")
    public Result deleteMessageList(@RequestParam List<String> messageIds) {
        messageService.deleteMessageList(messageIds);
        return Result.success("消息删除成功");
    }
    //删除用户消息
    @RequestMapping("/deleteUserMessage")
    public Result deleteUserMessage(@RequestParam String userId) {
        messageService.deleteUserMessage(userId);
        return Result.success("消息删除成功");
    }

    // 添加新的已读接口
    @PostMapping("/read/order")
    public Result markOrderMessagesAsRead() {
        int count = messageService.markOrderMessagesAsRead();
        return Result.success("标记" + count + "条订单消息为已读");
    }
    
    @PostMapping("/read/system")
    public Result markSystemMessagesAsRead() {
        int count = messageService.markSystemMessagesAsRead();
        return Result.success("标记" + count + "条系统消息为已读");
    }
    
    @PostMapping("/read/user")
    public Result markUserMessagesAsRead(@RequestParam String userId) {
        System.out.println(userId);
        int count = messageService.markUserMessagesAsRead(userId);
        return Result.success("标记" + count + "条用户消息为已读");
    }
    
    @PostMapping("/read/all")
    public Result markAllMessagesAsRead() {
        int count = messageService.markAllMessagesAsRead();
        return Result.success("标记" + count + "条消息为已读");
    }
}

