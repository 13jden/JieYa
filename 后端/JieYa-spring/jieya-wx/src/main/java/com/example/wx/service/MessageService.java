package com.example.wx.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.WxDto.MessageListDto;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.pojo.Comment;
import com.example.common.pojo.Message;

import java.util.List;

/**
 * <p>
 *  用户消息服务接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-27
 */
public interface MessageService extends IService<Message> {
    
    /**
     * 获取用户消息会话列表
     */
    Page<MessageListDto> getUserMessagesList(String currentUserId, int pageNum, int pageSize);

    
    /**
     * 获取与特定对象的聊天记录
     */
    Page<Message> getChatMessages(String currentUserId,String targetId, int pageNum, int pageSize);
    
    /**
     * 获取系统消息
     */
    Page<UserMessageDto> getSystemMessages(String currentUserId, int pageNum, int pageSize);
    
    /**
     * 获取订单消息
     */
    Page<UserMessageDto> getOrderMessages(String currentUserId, int pageNum, int pageSize);
    
    /**
     * 标记聊天消息为已读
     */
    int markChatMessagesAsRead(String currentUserId,String targetId);
    
    /**
     * 标记系统消息为已读
     */
    int markSystemMessagesAsRead(String currentUserId);
    
    /**
     * 标记订单消息为已读
     */
    int markOrderMessagesAsRead(String currentUserId);
    
    /**
     * 删除消息
     */
    void deleteMessage(String messageId);
    
    /**
     * 批量删除消息
     */
    void batchDeleteMessages(List<String> messageIds);
    
    /**
     * 获取未读聊天消息数量
     */
    int getUnreadChatCount(String currentUserId);
    
    /**
     * 获取未读系统消息数量
     */
    int getUnreadSystemCount(String currentUserId);
    
    /**
     * 获取未读订单消息数量
     */
    int getUnreadOrderCount(String currentUserId);


    int getUnreadAllCount(String currentUserId);
}
