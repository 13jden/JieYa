package com.example.admin.websocket;

import com.example.common.pojo.Message;
import com.example.common.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(MyWebSocketHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<WebSocketSession, String> sessionUserMap = new ConcurrentHashMap<>();
    
    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接已建立: {}", session.getId());
        
        // 从URL参数提取token
        String token = extractTokenFromSession(session);

        if (token != null && jwtUtil.validateToken(token)) {
            // 获取用户ID
            String userId = jwtUtil.getUserId(token);
            // 将用户ID和会话关联
            sessionUserMap.put(session, userId);
            // 注册到WebSocketService
            webSocketService.registerUserSession(userId, session);

            // 发送连接成功消息
            Message connectMessage = new Message();
            connectMessage.setType("CONNECT");
            connectMessage.setUser("system");
            connectMessage.setContent("WebSocket连接成功");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(connectMessage)));
        } else {
            log.warn("WebSocket连接验证失败，关闭连接: {}", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到WebSocket消息: {}", payload);
        
        try {
            // 先检查是否是心跳消息
            if (payload.contains("\"type\":\"heartbeat\"")) {
                handleHeartbeat(session);
                return;
            }
            
            // 处理其他类型消息
            String userId = sessionUserMap.get(session);
            if (userId == null) {
                log.warn("未找到会话对应的用户ID: {}", session.getId());
                return;
            }
            
            // 解析消息
            Message msg = objectMapper.readValue(payload, Message.class);
            
            // 设置消息发送者
            if (msg.getUser() == null || msg.getUser().isEmpty()) {
                msg.setUser(userId);
            }
            
            // 处理消息
            webSocketService.sendMessage(msg);
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage());
            // 发送错误响应
            Message errorMsg = new Message();
            errorMsg.setType("ERROR");
            errorMsg.setUser("system");
            errorMsg.setContent("消息处理失败: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
        }
    }
    
    // 处理心跳消息
    private void handleHeartbeat(WebSocketSession session) throws Exception {
        // 更新用户最后活跃时间
        String userId = sessionUserMap.get(session);
        if (userId != null) {
            // 通知WebSocketService更新用户心跳
            webSocketService.updateUserLastActiveTime(userId);
        }
        
        // 发送心跳响应
        session.sendMessage(new TextMessage("{\"type\":\"heartbeat_response\",\"time\":" + System.currentTimeMillis() + "}"));
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        String userId = sessionUserMap.get(session);
        if (userId != null) {
            webSocketService.removeUserSession(userId);
            sessionUserMap.remove(session);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket连接已关闭: {}, 原因: {}", session.getId(), status.getReason());
        String userId = sessionUserMap.get(session);
        if (userId != null) {
            webSocketService.removeUserSession(userId);
            sessionUserMap.remove(session);
        }
    }
    
    // 从WebSocket会话中提取token
    private String extractTokenFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                if (pair.startsWith("token=")) {
                    return pair.substring(6); // "token=".length() == 6
                }
            }
        }
        return null;
    }
}
