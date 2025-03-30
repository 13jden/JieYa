package com.example.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.mapper.MessageMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.service.MessageService;
import com.example.common.adminDto.AdminMessageDto;
import com.example.common.pojo.Message;
import com.example.common.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-27
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${host.url}")
    private String hostUrl;
    
    @Override
    public Page<AdminMessageDto> getMessagesListByToUser(int pageNum, int pageSize) {
        Page<AdminMessageDto> page = new Page<>(pageNum, pageSize);
        
        // 获取消息统计信息
        List<AdminMessageDto> messageDtos = messageMapper.selectUserMessageList(page);
        
        // 添加空值检查
        if (messageDtos != null && !messageDtos.isEmpty()) {
            // 补充用户信息
            for (AdminMessageDto dto : messageDtos) {
                if (dto == null) continue;
              
                String currentUser = "admin"; 
                String otherUser = currentUser.equals(dto.getUser()) ? dto.getToUser() : dto.getUser();
                
                if (otherUser == null) continue;
                
                User user = userMapper.selectById(otherUser);
                if (user != null) {
                    dto.setUser(otherUser);
                    dto.setUserName(user.getNickName());
                    dto.setAvatarUrl(hostUrl + "/images/avatar/" + user.getAvatar());
                }
                
                // 内容已经在查询中获取
            }
        } else {
            messageDtos = new ArrayList<>();
        }
        
        page.setRecords(messageDtos);
        return page;
    }

    @Override
    public Page<Message> getMessagesItemListByToUser(String userId, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        // 修改为同时传入当前用户ID和对话用户ID
        String currentUser = "admin"; // 假设当前登录的是管理员
        return messageMapper.selectUserMessageItemList(page, currentUser, userId);
    }
    @Override
    public Page<AdminMessageDto> getOrderMessage(int pageNum, int pageSize) {
        Page<AdminMessageDto> page = new Page<>(pageNum, pageSize);
        return messageMapper.selectOrderMessage(page);
    }
    @Override
    public Page<AdminMessageDto> getSystemMessage(int pageNum, int pageSize) {
        Page<AdminMessageDto> page = new Page<>(pageNum, pageSize);
        return messageMapper.selectSystemMessage(page);
    }

    @Override
    public void deleteMessage(String messageId) {
        messageMapper.deleteById(messageId);
    }

    @Override
    public void deleteMessageList(List<String> messageIds) {
        messageMapper.deleteBatchIds(messageIds);
    }

    @Override
    public void deleteUserMessage(String userId) {
        messageMapper.deleteUserMessage(userId);
    }

    @Override
    public Integer getUserMessageCount() {
        return messageMapper.selectUserMessageCount();
    }

    @Override
    public Integer getOrderMessageCount() {
        return messageMapper.selectOrderMessageCount();
    }

    @Override
    public Integer getSystemMessageCount() {
        return messageMapper.selectSystemMessageCount();
    }

    @Override
    public int markOrderMessagesAsRead() {
        return messageMapper.markOrderMessagesAsRead();
    }

    @Override
    public int markSystemMessagesAsRead() {
        return messageMapper.markSystemMessagesAsRead();
    }

    @Override
    public int markUserMessagesAsRead(String userId) {
        return messageMapper.markUserMessagesAsRead(userId);
    }

    @Override
    public int markAllMessagesAsRead() {
        return messageMapper.markAllMessagesAsRead();
    }
}
