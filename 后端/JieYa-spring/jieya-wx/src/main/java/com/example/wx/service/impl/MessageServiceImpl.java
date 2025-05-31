package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.WxDto.MessageListDto;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.pojo.Comment;
import com.example.common.pojo.Message;
import com.example.common.pojo.User;
import com.example.common.utils.CopyTools;
import com.example.wx.mapper.MessageMapper;
import com.example.wx.mapper.UserMapper;
import com.example.wx.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public Page<MessageListDto> getUserMessagesList(String currentUserId, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        IPage<Message> messagePage = messageMapper.getUserMessageList(page, currentUserId);

        List<MessageListDto> dtoList = new ArrayList<>();

        for (Message message : messagePage.getRecords()) {
            MessageListDto messageListDto = CopyTools.copy(message, MessageListDto.class);
            messageListDto.setLastContent(message.getContent());

            // 默认未读消息数为0
            messageListDto.setNotReadCount(0);
            
            // 判断消息类型
            if ("ORDER-USER".equals(message.getType())) {
                messageListDto.setUserName("订单消息");
                // 查询订单系统发来的未读消息数
                int unreadCount = messageMapper.countUnreadMessages(currentUserId, "order");
                messageListDto.setNotReadCount(unreadCount);
            } else if ("SYSTEM-USER".equals(message.getType())) {
                messageListDto.setUserName("系统消息");
                // 查询系统发来的未读消息数
                int unreadCount = messageMapper.countUnreadMessages(currentUserId, "system");
                messageListDto.setNotReadCount(unreadCount);
            } else if ("USER-USER".equals(message.getType())) {
                String otherUserId;
                if (currentUserId.equals(message.getUser())) {
                    otherUserId = message.getToUser();
                } else {
                    otherUserId = message.getUser();
                }
                
                User user = userMapper.selectById(otherUserId);
                if (user != null) {
                    messageListDto.setAvatarUrl(hostUrl + "/images/avatar/" + user.getAvatar());
                    messageListDto.setUserName(user.getNickName());
                    messageListDto.setUserId(user.getUserId());
                    
                    // 查询来自该用户的未读消息数
                    int unreadCount = messageMapper.countUnreadMessagesFromUser(currentUserId, otherUserId);
                    messageListDto.setNotReadCount(unreadCount);
                }
            } else {
                messageListDto.setUserName("管理员消息");
                // 查询管理员发来的未读消息数
                int unreadCount = messageMapper.countUnreadMessages(currentUserId, "admin");
                messageListDto.setNotReadCount(unreadCount);
            }

            dtoList.add(messageListDto);
        }

        Page<MessageListDto> resultPage = new Page<>(pageNum, pageSize, messagePage.getTotal());
        resultPage.setRecords(dtoList);

        return resultPage;
    }



    /**
     * 获取通知消息（系统和订单消息）
     */
    private List<MessageListDto> getNotificationMessages(String userId) {
        // 查询系统消息
        List<UserMessageDto> systemMessages = messageMapper.selectLatestSystemMessages(userId);
        // 查询订单消息
        List<UserMessageDto> orderMessages = messageMapper.selectLatestOrderMessages(userId);
        
        List<MessageListDto> result = new ArrayList<>();
        
        // 转换系统消息
        if (systemMessages != null && !systemMessages.isEmpty()) {
            for (UserMessageDto dto : systemMessages) {
                MessageListDto listDto = convertToMessageListDto(dto);
                listDto.setUserName("系统通知");
                listDto.setAvatarUrl(hostUrl + "/images/system_avatar.png");
                result.add(listDto);
            }
        }
        
        // 转换订单消息
        if (orderMessages != null && !orderMessages.isEmpty()) {
            for (UserMessageDto dto : orderMessages) {
                MessageListDto listDto = convertToMessageListDto(dto);
                listDto.setUserName("订单通知");
                listDto.setAvatarUrl(hostUrl + "/images/order_avatar.png");
                result.add(listDto);
            }
        }
        
        return result;
    }
    
    /**
     * 获取用户消息（好友消息）
     */
    private List<MessageListDto> getUserMessages(String userId) {
        List<UserMessageDto> userMessages = messageMapper.selectLatestUserMessages(userId);
        List<MessageListDto> result = new ArrayList<>();
        
        if (userMessages != null && !userMessages.isEmpty()) {
            for (UserMessageDto dto : userMessages) {
                // 获取对话对象ID
                String otherUserId = userId.equals(dto.getUser()) ? dto.getToUser() : dto.getUser();
                
                // 获取用户信息
                User user = userMapper.selectById(otherUserId);
                if (user != null) {
                    MessageListDto listDto = convertToMessageListDto(dto);
                    listDto.setUserName(user.getNickName());
                    listDto.setAvatarUrl(hostUrl + "/images/avatar/" + user.getAvatar());
                    result.add(listDto);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取互动消息
     */
    private List<MessageListDto> getInteractionMessages(String userId) {
        List<UserMessageDto> interactionMessages = messageMapper.selectLatestInteractionMessages(userId);
        List<MessageListDto> result = new ArrayList<>();
        
        if (interactionMessages != null && !interactionMessages.isEmpty()) {
            for (UserMessageDto dto : interactionMessages) {
                // 获取互动用户信息
                User user = userMapper.selectById(dto.getUser());
                if (user != null) {
                    MessageListDto listDto = convertToMessageListDto(dto);
                    listDto.setUserName(user.getNickName());
                    listDto.setAvatarUrl(hostUrl + "/images/avatar/" + user.getAvatar());
                    result.add(listDto);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 转换UserMessageDto为MessageListDto
     */
    private MessageListDto convertToMessageListDto(UserMessageDto dto) {
        MessageListDto listDto = new MessageListDto();
        listDto.setLastContent(dto.getContent());
        listDto.setType(dto.getType());
        listDto.setTime(dto.getTime());
        listDto.setFileUrl(dto.getFileUrl());
        listDto.setNotReadCount(dto.getNotReadCount());
        return listDto;
    }

//    @Override
//    public Page<UserMessageDto> getChatMessage(String currentUserId, int pageNum, int pageSize) {
//        Page<UserMessageDto> page = new Page<>(pageNum, pageSize);
//
//        List<UserMessageDto> messages = messageMapper.selectUserMessagesList(page, currentUserId);
//
//        if (messages == null) {
//            messages = new ArrayList<>();
//        }
//
//        page.setRecords(messages);
//        return page;
//    }

    @Override
    public Page<Message> getChatMessages(String currentUserId, String targetId, int pageNum, int pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        // 查询后自动将聊天记录标记为已读
        page = messageMapper.selectChatMessages(page, currentUserId, targetId);
        
        // 处理文件URL，添加前缀
        for (Message message : page.getRecords()) {
            if (message.getFileUrl() != null && !message.getFileUrl().isEmpty()) {
                message.setFileUrl(hostUrl + "/files/message/" + message.getFileUrl());
            }
        }
        
        markChatMessagesAsRead(currentUserId, targetId);
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

    @Override
    public int getUnreadAllCount(String currentUserId) {
        return messageMapper.getUnreadAllCount(currentUserId);
    }


}
