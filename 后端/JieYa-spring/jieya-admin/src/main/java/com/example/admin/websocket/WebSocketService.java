package com.example.admin.websocket;

import com.example.common.pojo.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WebSocketService {
    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储所有活跃的WebSocket会话
    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();
    private static final Map<String, WebSocketSession> wxSessions = new ConcurrentHashMap<>();
    
    // 用户最后活跃时间映射
    private static final Map<String, Long> userLastActiveMap = new ConcurrentHashMap<>();
    // 心跳超时时间（毫秒），默认30秒
    private static final long HEARTBEAT_TIMEOUT = 30000;
    
    // 注册用户会话
    public void registerUserSession(String userId, WebSocketSession session) {
        userSessions.put(userId, session);
        // 记录用户活跃时间
        updateUserLastActiveTime(userId);
        log.info("用户[{}]已连接WebSocket", userId);

        if ("admin".equals(userId)) {
            adminSessions.put(userId, session);
            log.info("管理员已连接WebSocket");
        } else {
            // 非admin用户加入wx会话组
            wxSessions.put(userId, session);
            log.info("微信用户[{}]已连接WebSocket", userId);
        }
    }
    
    // 移除用户会话
    public void removeUserSession(String userId) {
        userSessions.remove(userId);
        adminSessions.remove(userId);
        wxSessions.remove(userId);
        // 移除用户活跃记录
        userLastActiveMap.remove(userId);
        log.info("用户[{}]已断开WebSocket连接", userId);
    }
    
    // 更新用户最后活跃时间
    public void updateUserLastActiveTime(String userId) {
        userLastActiveMap.put(userId, System.currentTimeMillis());
        log.debug("用户[{}]心跳更新", userId);
    }
    
    // 获取在线用户列表
    public List<String> getOnlineUsers() {
        long now = System.currentTimeMillis();
        return userLastActiveMap.entrySet().stream()
            .filter(entry -> (now - entry.getValue()) < HEARTBEAT_TIMEOUT)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    // 获取在线用户数量
    public int getOnlineUserCount() {
        return getOnlineUsers().size();
    }
    
    // 获取在线管理员列表
    public List<String> getOnlineAdmins() {
        return getOnlineUsers().stream()
            .filter(userId -> "admin".equals(userId))
            .collect(Collectors.toList());
    }
    
    // 获取在线微信用户列表
    public List<String> getOnlineWxUsers() {
        return getOnlineUsers().stream()
            .filter(userId -> !"admin".equals(userId))
            .collect(Collectors.toList());
    }
    
    // 判断用户是否在线
    public boolean isUserOnline(String userId) {
        Long lastActiveTime = userLastActiveMap.get(userId);
        if (lastActiveTime == null) {
            return false;
        }
        return (System.currentTimeMillis() - lastActiveTime) < HEARTBEAT_TIMEOUT;
    }
    
    // 清理不活跃用户
    public void cleanupInactiveUsers() {
        long now = System.currentTimeMillis();
        List<String> inactiveUsers = userLastActiveMap.entrySet().stream()
            .filter(entry -> (now - entry.getValue()) > HEARTBEAT_TIMEOUT)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        for (String userId : inactiveUsers) {
            log.info("清理不活跃用户: {}", userId);
            WebSocketSession session = userSessions.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (IOException e) {
                    log.error("关闭不活跃用户会话失败: {}", e.getMessage());
                }
            }
            removeUserSession(userId);
        }
        
        log.info("当前在线用户数: {}", getOnlineUserCount());
    }
    
    // 发送消息到admin主题
    public void sendToAdmin(Message message) {
        // 设置消息来源
        message.setType(message.getType());
        log.info("发送消息到admin主题: {}", message.getContent());
        
        // 向所有admin会话发送消息
        broadcastToSessions(adminSessions.values().stream().toList(), message);
    }

    // 发送消息到wx主题
    public void sendToWx(Message message) {
        // 设置消息来源
        message.setType(message.getType());
        log.info("发送消息到wx主题: {}", message.getContent());
        
        // 向所有wx会话发送消息
        broadcastToSessions(wxSessions.values().stream().toList(), message);
    }

    // 发送消息给特定用户
    public void sendToUser(String userId, Message message) {
        log.info("发送消息给用户[{}]: {}", userId, message.getContent());
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            } catch (IOException e) {
                log.error("发送消息给用户[{}]失败: {}", userId, e.getMessage());
            }
        } else {
            log.warn("用户[{}]不在线或会话已关闭", userId);
        }
    }
    
    // 发送消息给多个用户
    public void sendToUsers(List<String> userIds, Message message) {
        log.info("发送消息给{}个用户", userIds.size());
        for (String userId : userIds) {
            sendToUser(userId, message);
        }
    }
    
    // 根据消息的toUser字段自动选择发送方式
    public void sendMessage(Message message) {
        String toUser = message.getToUser();
        
        if (toUser == null || toUser.isEmpty()) {
            // 没有指定接收者，发送到公共频道
            sendToWx(message);
        } else if ("admin".equals(toUser)) {
            // 发送到管理员频道
            sendToAdmin(message);
        } else {
            // 发送给特定用户
            sendToUser(toUser, message);
        }
    }
    
    // 发送系统通知
    public void sendSystemNotification(String userId, String content) {
        Message message = new Message();
        message.setUser("system");
        message.setToUser(userId);
        message.setContent(content);
        message.setType("NOTIFICATION");
        
        sendToUser(userId, message);
    }
    
    // 向多个会话广播消息
    private void broadcastToSessions(List<WebSocketSession> sessions, Message message) {
        if (sessions.isEmpty()) {
            log.warn("没有活跃的会话可接收消息");
            return;
        }
        
        try {
            TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(message));
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("向会话[{}]发送消息失败: {}", session.getId(), e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("序列化消息失败: {}", e.getMessage());
        }
    }
}