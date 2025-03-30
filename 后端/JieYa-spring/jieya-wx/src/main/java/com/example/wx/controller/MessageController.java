package com.example.wx.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.common.Result;
import com.example.common.pojo.Message;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import com.example.wx.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
    
    /**
     * 发送消息 - 只将消息放入Redis队列
     */
    @PostMapping("/send")
    public Result sendMessage(@NotEmpty String toUser,
                              @NotEmpty String content,
                              @NotEmpty String type,
                              String userId,
                              String fileUrl,
                              HttpServletRequest request) {
//        String token = request.getHeader("Authorization");
//        if (token == null) {
//            return Result.error("未登录");
//        }
//        String currentUserId = JwtUtil.getUsernameFromToken(token);
//
        Message message = new Message();
        message.setUser(userId);
        message.setTime(new Date());
        message.setToUser(toUser);
        message.setContent(content);
        message.setType(type);
        message.setStatus(0); // 未读状态
        
        if (fileUrl != null && !fileUrl.isEmpty()) {
            message.setFileUrl(fileUrl);
        }
        
        // 保存消息到数据库
        messageService.save(message);
        
        // 将消息放入队列由后端消费
        redisComponent.setMessage(message);
        
        return Result.success("消息发送成功");
    }
    
    /**
     * 获取用户消息列表
     */
    @GetMapping("/list/{pageNum}/{pageSize}")
    public Result getUserMessagesList(@PathVariable int pageNum, 
                                     @PathVariable int pageSize,
                                     HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return Result.error("未登录");
        }
        String currentUserId = JwtUtil.getUsernameFromToken(token);
        Page<UserMessageDto> page = messageService.getUserMessagesList(currentUserId, pageNum, pageSize);
        return Result.success(page);
    }
    
    /**
     * 获取系统消息
     */
    @GetMapping("/system/{pageNum}/{pageSize}")
    public Result getSystemMessages(@PathVariable int pageNum,
                                   @PathVariable int pageSize,
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
    @GetMapping("/chat/{targetId}/{pageNum}/{pageSize}")
    public Result getChatMessages(@PathVariable String targetId,
                                 @PathVariable int pageNum,
                                 @PathVariable int pageSize,
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
    @GetMapping("/order/{pageNum}/{pageSize}")
    public Result getOrderMessages(@PathVariable int pageNum,
                                  @PathVariable int pageSize,
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
    @PostMapping("/read/{targetId}")
    public Result markAsRead(HttpServletRequest request, @PathVariable String targetId) {
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
        
        int chatCount = messageService.getUnreadChatCount(currentUserId);
        int systemCount = messageService.getUnreadSystemCount(currentUserId);
        int orderCount = messageService.getUnreadOrderCount(currentUserId);
        
        UnreadCountDto countDto = new UnreadCountDto(chatCount, systemCount, orderCount);
        return Result.success(countDto);
    }
    
    /**
     * 未读消息数量DTO
     */
    private static class UnreadCountDto {
        private int chatCount;
        private int systemCount;
        private int orderCount;
        private int totalCount;
        
        public UnreadCountDto(int chatCount, int systemCount, int orderCount) {
            this.chatCount = chatCount;
            this.systemCount = systemCount;
            this.orderCount = orderCount;
            this.totalCount = chatCount + systemCount + orderCount;
        }
        
        // Getters
        public int getChatCount() { return chatCount; }
        public int getSystemCount() { return systemCount; }
        public int getOrderCount() { return orderCount; }
        public int getTotalCount() { return totalCount; }
    }
}

