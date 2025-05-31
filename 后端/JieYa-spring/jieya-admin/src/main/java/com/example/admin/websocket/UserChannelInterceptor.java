package com.example.admin.websocket;

import com.example.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
public class UserChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从请求头获取token
            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.get(0);
                // 使用JWT工具类验证token
                if (jwtUtil.validateToken(token)) {
                    // 从token中获取用户ID
                    String userId = JwtUtil.getUsernameFromToken(token);
                    
                    // 设置当前连接的用户信息
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return userId;
                        }
                    });
                }
            }
        }
        return message;
    }
} 