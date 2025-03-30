package com.example.admin.controller;

import com.example.common.common.Result;
import com.example.admin.websocket.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class OnlineController {
    @Autowired
    private WebSocketService webSocketService;
    
    @GetMapping("/online-users")
    public Result<Map<String, Object>> getOnlineUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", webSocketService.getOnlineUserCount());
        result.put("users", webSocketService.getOnlineUsers());
        return Result.success(result);
    }
    
    @GetMapping("/is-online")
    public Result<Map<String, Object>> isUserOnline(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("online", webSocketService.isUserOnline(userId));
        return Result.success(result);
    }

}