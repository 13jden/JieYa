package com.example.wx.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.pojo.Message;
import com.example.wx.mapper.MessageMapper;
import com.example.wx.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Override
    public Page<UserMessageDto> getUserMessagesList(String currentUserId, int pageNum, int pageSize) {
        Page<UserMessageDto> page = new Page<>(pageNum, pageSize);
        
        List<UserMessageDto> messages = messageMapper.selectUserMessagesList(page, currentUserId);
        
        if (messages == null) {
            messages = new ArrayList<>();
        }
        
        page.setRecords(messages);
        return page;
    }

    @Override
    public Page<Message> getChatMessages(String currentUserId,String targetId, int pageNum, int pageSize) {

        Page<Message> page = new Page<>(pageNum, pageSize);
        
        // 查询后自动将聊天记录标记为已读
        page = messageMapper.selectChatMessages(page, currentUserId, targetId);
        markChatMessagesAsRead(currentUserId,targetId);
        
        return page;
    }

    @Override
    public Page<UserMessageDto> getSystemMessages(String currentUserId, int pageNum, int pageSize) {
        Page<UserMessageDto> page = new Page<>(pageNum, pageSize);
        return messageMapper.selectSystemMessages(page, currentUserId);
    }

    @Override
    public Page<UserMessageDto> getOrderMessages(String currentUserId, int pageNum, int pageSize) {
        Page<UserMessageDto> page = new Page<>(pageNum, pageSize);
        return messageMapper.selectOrderMessages(page, currentUserId);
    }

    @Override
    public int markChatMessagesAsRead(String currentUserId,String targetId) {
        return messageMapper.markChatMessagesAsRead(currentUserId, targetId);
    }

    @Override
    public int markSystemMessagesAsRead(String currentUserId) {
        return messageMapper.markSystemMessagesAsRead(currentUserId);
    }

    @Override
    public int markOrderMessagesAsRead(String currentUserId) {
        return messageMapper.markOrderMessagesAsRead(currentUserId);
    }

    @Override
    public void deleteMessage(String messageId) {
        messageMapper.deleteById(messageId);
    }

    @Override
    public void batchDeleteMessages(List<String> messageIds) {
        messageMapper.deleteBatchIds(messageIds);
    }

    @Override
    public int getUnreadChatCount(String currentUserId) {
        return messageMapper.getUnreadChatCount(currentUserId);
    }

    @Override
    public int getUnreadSystemCount(String currentUserId) {
        return messageMapper.getUnreadSystemCount(currentUserId);
    }

    @Override
    public int getUnreadOrderCount(String currentUserId) {
        return messageMapper.getUnreadOrderCount(currentUserId);
    }
}
