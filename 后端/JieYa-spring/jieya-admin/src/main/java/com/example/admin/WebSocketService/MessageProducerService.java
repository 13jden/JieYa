package com.example.admin.WebSocketService;

import com.example.common.pojo.Message;
import com.example.common.redis.RedisComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class MessageProducerService {


    
    @Autowired
    private RedisComponent redisComponent;
    
    /**
     * 保存消息到数据库并添加到Redis队列
     */
    @Transactional
    public void produceMessage(Message message) {
        try {
            // 设置消息时间
            if (message.getTime() == null) {
                message.setTime(new Date());
            }
            //将消息添加到Redis队列
            redisComponent.setMessage(message);

        } catch (Exception e) {
            throw new RuntimeException("处理消息失败: " + e.getMessage(), e);
        }
    }
} 