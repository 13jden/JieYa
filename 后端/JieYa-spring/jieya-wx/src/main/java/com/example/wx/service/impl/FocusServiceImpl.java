package com.example.wx.service.impl;

import com.example.common.WxDto.ActiveMessageDto;
import com.example.common.constants.Constants;
import com.example.common.pojo.Focus;
import com.example.common.pojo.Message;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.wx.mapper.FocusMapper;
import com.example.wx.mapper.MessageMapper;
import com.example.wx.mapper.UserMapper;
import com.example.wx.service.FocusService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Service
public class FocusServiceImpl extends ServiceImpl<FocusMapper, Focus> implements FocusService {

    @Autowired
    private FocusMapper focusMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Object getFocusList(String userId) {
        return null;
    }

    @Override
    @Transactional
    public Object deleteFocus(String userId, String focusUserId) {
        // 检查是否已关注
        if (!focusMapper.checkFocus(userId, focusUserId)) {
            throw new IllegalArgumentException("未关注该用户，无法取消");
        }
        
        // 处理消息
        ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(
                Constants.REDIS_KEY_ACTIVE_MESSAGE + "FOCUS:" + userId + focusUserId);
        
        if (activeMessageDto != null) {
            // 删除系统到用户的消息
            messageMapper.deleteById(activeMessageDto.getId());
            
            // 删除用户到用户的感谢消息

            messageMapper.deleteById(activeMessageDto.getThankMessageId());

            
            // 删除Redis中的消息记录
            redisComponent.deleteUserActiveMessage(
                    Constants.REDIS_KEY_ACTIVE_MESSAGE + "FOCUS:" + userId + focusUserId);
        }
        
        // 删除关注记录
        focusMapper.deleteFocus(userId, focusUserId);
        
        return "取消关注成功";
    }

    @Override
    @Transactional
    public Object addFocus(String userId, String focusUserId) {
        // 检查是否已关注
        if (focusMapper.checkFocus(userId, focusUserId)) {
            throw new IllegalArgumentException("已经关注过该用户");
        }
        
        // 创建关注记录
        Focus focus = new Focus();
        focus.setUserId(userId);
        focus.setFocusId(focusUserId);
        focus.setFocusTime(new Date());
        focusMapper.insert(focus);
        
        // 获取用户名称
        String userName = userMapper.getUserName(userId);
        String focusUserName = userMapper.getUserName(focusUserId);
        
        // 查询是否有合并消息
        ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(
                Constants.REDIS_KEY_ACTIVE_MESSAGE + "FOCUS:" + userId + focusUserId);
        
        if (activeMessageDto != null) {
            // 有消息则更新
            Message message = CopyTools.copy(activeMessageDto, Message.class);
            message.setTime(new Date());
            messageMapper.updateById(message);
        } else {
            // 创建系统到用户的消息
            Message message1 = new Message();
            message1.setContent(userName + "关注了你");
            message1.setUser("system");
            message1.setToUser(focusUserId);
            message1.setType("SYSTEM-USER");
            message1.setTime(new Date());
            messageMapper.insert(message1);
            
            // 创建用户到用户的感谢消息
            Message message2 = new Message();
            message2.setContent("谢谢你的关注");
            message2.setUser(focusUserId);
            message2.setToUser(userId);
            message2.setType("USER-USER");
            message2.setTime(new Date());
            messageMapper.insert(message2);
            
            // 保存消息ID到Redis用于后续操作
            activeMessageDto = CopyTools.copy(message1, ActiveMessageDto.class);
            activeMessageDto.setThankMessageId(message2.getId());
            redisComponent.setUserActiveMessage(
                    Constants.REDIS_KEY_ACTIVE_MESSAGE + "FOCUS:" + userId + focusUserId, 
                    activeMessageDto);
            redisComponent.setMessage(message1);
            redisComponent.setMessage(message2);
        }
        
        return "关注成功";
    }

    @Override
    public Object getFansList(String userId) {
        return null;
    }
}
