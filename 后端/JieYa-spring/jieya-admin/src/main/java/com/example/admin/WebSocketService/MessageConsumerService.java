package com.example.admin.WebSocketService;

import com.example.common.pojo.Message;
import com.example.common.redis.RedisComponent;
import com.example.admin.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumerService {

    @Autowired
    private RedisComponent redisComponent;
    
    @Autowired
    private WebSocketService webSocketService;

    
    /**
     * 定时从Redis队列中获取消息并发送
     * 每100毫秒执行一次
     */
    @Scheduled(fixedRate = 100)
    public void consumeMessages() {
        try {
            // 从Redis队列右侧弹出消息(RPOP操作)
            Message message = redisComponent.getMessage();
            // 增加非空检查
            if (message != null) {
                dispatchMessage(message);
            }
        } catch (Exception e) {
            // 记录错误但不中断处理
            System.err.println("处理消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 根据消息类型和接收者分发消息
     */
    private void dispatchMessage(Message message) {
        // 获取接收者
        String toUser = message.getToUser();
        message.setId((long) (Math.random()*1000));
        // 根据接收者类型发送消息
        if ("admin".equals(toUser)) {
            // 发送到管理员主题
            System.out.println("马上发生给admin:"+message.getContent());

            webSocketService.sendToAdmin(message);
        } else if (toUser != null && !toUser.isEmpty()) {
            // 发送到特定用户
            System.out.println("马上发生给user:"+message.getContent());
            webSocketService.sendToUser(toUser, message);
        } else {
            // 发送到微信主题
            System.out.println("马上发生给wx:"+message.getContent());
            webSocketService.sendToWx(message);
        }

    }
} 