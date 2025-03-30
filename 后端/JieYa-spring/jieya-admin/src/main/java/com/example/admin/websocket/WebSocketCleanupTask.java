package com.example.admin.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class WebSocketCleanupTask {
    @Autowired
    private WebSocketService webSocketService;
    
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void cleanupInactiveUsers() {
        webSocketService.cleanupInactiveUsers();
    }
}
