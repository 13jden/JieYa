package com.example.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.pojo.Message;
import com.example.common.adminDto.AdminMessageDto;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-27
 */
public interface MessageService extends IService<Message> {
    //获取用户发送的用户列表
    Page<AdminMessageDto> getMessagesListByToUser(int pageNum, int pageSize);
    //获取用户发送的消息列表
    Page<Message> getMessagesItemListByToUser(String userId, int pageNum, int pageSize);
    
    //获取系统消息
    Page<AdminMessageDto> getSystemMessage(int pageNum, int pageSize);
    //获取订单消息
    Page<AdminMessageDto> getOrderMessage(int pageNum, int pageSize);
    
    //删除消息
    void deleteMessage(String messageId);
    //批量删除消息
    void deleteMessageList(List<String> messageIds);
    //删除用户消息
    void deleteUserMessage(String userId);

    Integer getUserMessageCount();
    Integer getOrderMessageCount();
    Integer getSystemMessageCount();
    
    // 新增已读接口
    int markOrderMessagesAsRead();
    int markSystemMessagesAsRead();
    int markUserMessagesAsRead(String userId);
    int markAllMessagesAsRead();

    /**
     * 获取用户消息类型列表
     * @return 系统消息类型列表
     */
    List<Map<String, String>> getMessage2UserType();
}