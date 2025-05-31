package com.example.wx.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.MessageListDto;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.common.Result;
import com.example.common.pojo.Message;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import com.example.wx.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *  用户消息控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-27
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private RedisComponent redisComponent;

    @Value("${host.url}")
    private String hostUrl;
    
    /**
     * 发送消息 - 只将消息放入Redis队列
     */
    @PostMapping("/send")
    public Result sendMessage(@NotEmpty String toUser,
                              @NotEmpty String content,
                              @NotEmpty String type,
                              String fileUrl,
                              HttpServletRequest request) {
       String token = request.getHeader("Authorization");
       if (token == null) {
           return Result.error("未登录");
       }
       String currentUserId = JwtUtil.getUsernameFromToken(token);
//
        Message message = new Message();
        message.setUser(currentUserId);
        message.setTime(new Date());
        message.setToUser(toUser);
        message.setContent(content);
        message.setType(type);
        message.setStatus(0); // 未读状态
        System.out.println(fileUrl);

        if (fileUrl != null && !fileUrl.isEmpty() && !fileUrl.equals("undefined")) {
            // 使用正则表达式匹配日期目录和文件名部分
            Pattern pattern = Pattern.compile(".*/files/message/(\\d+/[^/]+)");
            Matcher matcher = pattern.matcher(fileUrl);
            
            if (matcher.find()) {
                // 提取符合格式的日期目录和文件名（例如：20250415/xxxx.jpg）
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
            
        // 保存消息到数据库
        messageService.save(message);
        
        // 将消息放入队列由后端消费
        redisComponent.setMessage(message);
        
        return Result.success("消息发送成功");
    }
    
    /**
     * 获取消息列表
     */
    @GetMapping("/getList")
    public Result getUserMessagesList(@RequestParam int pageNum,
                                     @RequestParam int pageSize,
                                     HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        Page<MessageListDto> page = messageService.getUserMessagesList(currentUserId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取系统消息
     */
    @GetMapping("/system")
    public Result getSystemMessages(@RequestParam int pageNum,
                                   @RequestParam int pageSize,
                                   HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        Page<UserMessageDto> page = messageService.getSystemMessages(currentUserId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取与特定用户的聊天记录
     */
    @GetMapping("/chat")
    public Result getChatMessages(@RequestParam String targetId,
                                 @RequestParam int pageNum,
                                 @RequestParam int pageSize,
                                 HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        Page<Message> page = messageService.getChatMessages(currentUserId, targetId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取订单消息
     */
    @GetMapping("/order")
    public Result getOrderMessages(@RequestParam int pageNum,
                                  @RequestParam int pageSize,
                                  HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        Page<UserMessageDto> page = messageService.getOrderMessages(currentUserId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    public Result markAsRead(HttpServletRequest request, @RequestParam String targetId) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        int count = messageService.markChatMessagesAsRead(currentUserId, targetId);
        return Result.success("已标记" + count + "条消息为已读");
    }
    
    /**
     * 标记系统消息为已读
     */
    @PostMapping("/system/read")
    public Result markSystemMessagesAsRead(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        int count = messageService.markSystemMessagesAsRead(currentUserId);
        return Result.success("已标记" + count + "条系统消息为已读");
    }
    
    /**
     * 标记订单消息为已读
     */
    @PostMapping("/order/read")
    public Result markOrderMessagesAsRead(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        int count = messageService.markOrderMessagesAsRead(currentUserId);
        return Result.success("已标记" + count + "条订单消息为已读");
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public Result deleteMessage(@PathVariable String messageId) {
        messageService.deleteMessage(messageId);
        return Result.success("删除成功");
    }
    
    /**
     * 批量删除消息
     */
    @DeleteMapping("/batch")
    public Result batchDeleteMessages(@RequestBody List<String> messageIds) {
        messageService.batchDeleteMessages(messageIds);
        return Result.success("批量删除成功");
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public Result getUnreadCount(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);

        int allCount = messageService.getUnreadAllCount(currentUserId);
        return Result.success(allCount);
    }
    

}

