package com.example.wx.service.impl;

import com.example.wx.mapper.CommentMapper;
import com.example.common.WxDto.ActiveMessageDto;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.constants.Constants;
import com.example.common.pojo.*;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.wx.mapper.UserMapper;
import com.example.wx.mapper.MessageMapper;
import com.example.wx.mapper.NodeMapper;
import com.example.wx.mapper.UserActiveMapper;
import com.example.wx.service.UserActiveService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Service
public class UserActiveServiceImpl extends ServiceImpl<UserActiveMapper, UserActive> implements UserActiveService {

    @Autowired
    private UserActiveMapper userActiveMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private CommentServiceImpl commentService;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public String getCollectList(String userId) {
        return null;
    }

    @Override
    @Transactional
    public String addLike(String userId, String noteId,String toUserId  ) {
            if (userActiveMapper.checkLike(userId,noteId)){
                throw new IllegalArgumentException("不能重复点赞");
            }
            UserActive userActive = new UserActive();
            userActive.setActive("LIKE");
            userActive.setUserId(userId);
            userActive.setNodeId(noteId);
            userActive.setTime(new Date());
            userActive.setToUserId(toUserId);
            userActiveMapper.insert(userActive);
            nodeMapper.addLikeCount(noteId);
            userMapper.addLikeCount(toUserId);
            if(userId == toUserId){
                return "点赞成功";
            }
            ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE+"LIKE:"+noteId+toUserId);
            String toUserName = userMapper.getUserName(userId);
            if(!userId.equals(toUserId)){
                if(activeMessageDto!=null){
                    activeMessageDto.getUserName().add(toUserName);
                    Message message = CopyTools.copy(activeMessageDto,Message.class);
                    message.setTime(new Date());
                    activeMessageDto.setPostTime(messageMapper.getTimeById(activeMessageDto.getId()));//取上一次的消息时间，方便取消改回去
                    activeMessageDto.setUserCount(activeMessageDto.getUserCount()+1);
                    activeMessageDto.setContent(toUserName+","+activeMessageDto.getUserName().get(activeMessageDto.getUserName().size()-1)+"等"+activeMessageDto.getUserCount()+"位用户给你点赞了你的笔记");
                    messageMapper.updateById(message);
                    redisComponent.setUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE+"LIKE:"+noteId+toUserId,activeMessageDto);
                    redisComponent.setMessage(message);
                }else{
                    Message message = new Message();
                    message.setContent(toUserName+"点赞了你的笔记");
                    message.setUser("system");
                    message.setToUser(toUserId);
                    message.setType("SYSTEM-USER");
                    message.setTime(new Date());
                    message.setNoteId(noteId);
                    messageMapper.insert(message);
                    activeMessageDto = CopyTools.copy(message,ActiveMessageDto.class);
                    List<String> userNameList = new ArrayList<>();
                    activeMessageDto.setUserName(userNameList);
                    activeMessageDto.setUserCount(1);
                    redisComponent.setUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE+"LIKE:"+noteId+toUserId,activeMessageDto);
                    redisComponent.setMessage(message);
                }
            }

            return "点赞成功";
    }

    @Override
    @Transactional
    public String deleteLike(String userId, String noteId,String toUserId) {
        if (!userActiveMapper.checkLike(userId,noteId)){
            throw new IllegalArgumentException("点赞取消失败");
        }
        ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE+"LIKE:"+noteId+toUserId);

        if(activeMessageDto!=null){
            Message message = CopyTools.copy(activeMessageDto,Message.class);
            String userName1 = null;
            String userName2 = null;
            String userName3 = null;
            if(activeMessageDto.getUserName().size()>0){
                userName1 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size()-1);
            }
            if(activeMessageDto.getUserName().size()>1){
                userName2 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size()-2);
            }
            if(activeMessageDto.getUserName().size()>2){
                userName3 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size()-3);//消息回退一次
            }
            if(userName2==null){//只有一条点赞消息
                messageMapper.deleteById(message);
                redisComponent.deleteUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE+"LIKE:"+noteId+toUserId);
            }else if(userName3==null){//有两个人点赞，改回一个人在点赞
                message.setContent(userName2+"点赞了你的笔记");
                message.setTime(activeMessageDto.getPostTime());//回退时间
                messageMapper.updateById(message);
                redisComponent.deleteUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE+"LIKE:"+noteId+toUserId);//直接删除
            }else{//多人点赞
                message.setContent(userName2+","+userName1+"等"+activeMessageDto.getUserCount()+"位用户给你点赞了你的笔记");
                message.setTime(activeMessageDto.getPostTime());//回退时间
                messageMapper.updateById(message);
                redisComponent.deleteUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE+"LIKE:"+noteId+toUserId);//直接删除
            }
        }
        System.out.println("准备删除ing");
        userActiveMapper.deleteLike( userId,noteId);
        nodeMapper.reduceLikeCount(noteId);
        return "取消点赞成功";

    }

    @Override
    @Transactional
    public String addCollect(String userId, String noteId) {
        if (userActiveMapper.checkCollect(userId, noteId)) {
            throw new IllegalArgumentException("不能重复收藏");
        }
        
        // 获取笔记所属用户
        Node node = nodeMapper.selectById(noteId);
        if (node == null) {
            throw new IllegalArgumentException("笔记不存在");
        }
        String toUserId = node.getUserId();
        
        // 创建收藏记录
        UserActive userActive = new UserActive();
        userActive.setActive("COLLECT");
        userActive.setUserId(userId);
        userActive.setNodeId(noteId);
        userActive.setTime(new Date());
        userActive.setToUserId(toUserId);
        userActiveMapper.insert(userActive);
        nodeMapper.addCollectCount(noteId);
        userMapper.addCollectCount(toUserId);
        // 如果是自己的笔记，不发送消息
        if (userId.equals(toUserId)) {
            return "收藏成功";
        }
        
        // 查询是否有合并消息
        ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(
                Constants.REDIS_KEY_ACTIVE_MESSAGE + "COLLECT:" + noteId + toUserId);
        String toUserName = userMapper.getUserName(userId);
        if(!userId.equals(toUserId)){
            if (activeMessageDto != null) {
                // 有消息则合并
                activeMessageDto.getUserName().add(toUserName);
                Message message = CopyTools.copy(activeMessageDto, Message.class);
                message.setTime(new Date());
                activeMessageDto.setPostTime(messageMapper.getTimeById(activeMessageDto.getId()));
                activeMessageDto.setUserCount(activeMessageDto.getUserCount() + 1);
                activeMessageDto.setContent(toUserName + "," +
                        activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 1) +
                        "等" + activeMessageDto.getUserCount() + "位用户收藏了你的笔记");
                messageMapper.updateById(message);
                redisComponent.setUserActiveMessage(
                        Constants.REDIS_KEY_ACTIVE_MESSAGE + "COLLECT:" + noteId + toUserId,
                        activeMessageDto);
                redisComponent.setMessage(message);
            } else {
                // 无消息则创建新消息
                Message message = new Message();
                message.setContent(toUserName + "收藏了你的笔记");
                message.setUser("system");
                message.setToUser(toUserId);
                message.setType("SYSTEM-USER");
                message.setTime(new Date());
                message.setNoteId(noteId);
                messageMapper.insert(message);

                activeMessageDto = CopyTools.copy(message, ActiveMessageDto.class);
                List<String> userNameList = new ArrayList<>();
                userNameList.add(toUserName);
                activeMessageDto.setUserName(userNameList);
                activeMessageDto.setUserCount(1);
                redisComponent.setUserActiveMessage(
                        Constants.REDIS_KEY_ACTIVE_MESSAGE + "COLLECT:" + noteId + toUserId,
                        activeMessageDto);
                redisComponent.setMessage(message);
            }
        }

        
        return "收藏成功";
    }

    @Override
    @Transactional
    public String deleteCollect(String userId, String noteId) {
        if (!userActiveMapper.checkCollect(userId, noteId)) {
            throw new IllegalArgumentException("收藏取消失败");
        }
        
        // 获取笔记所属用户
        Node node = nodeMapper.selectById(noteId);
        if (node == null) {
            throw new IllegalArgumentException("笔记不存在");
        }
        String toUserId = node.getUserId();
        
        // 如果是自己的笔记，不处理消息
        if (!userId.equals(toUserId)) {
            // 处理合并消息
            ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(
                    Constants.REDIS_KEY_ACTIVE_MESSAGE + "COLLECT:" + noteId + toUserId);
            
            if (activeMessageDto != null) {
                Message message = CopyTools.copy(activeMessageDto, Message.class);
                String userName1 = null;
                String userName2 = null;
                String userName3 = null;
                
                if (activeMessageDto.getUserName().size() > 0) {
                    userName1 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 1);
                }
                if (activeMessageDto.getUserName().size() > 1) {
                    userName2 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 2);
                }
                if (activeMessageDto.getUserName().size() > 2) {
                    userName3 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 3);
                }
                
                if (userName2 == null) {
                    // 只有一条收藏消息，直接删除
                    messageMapper.deleteById(message.getId());
                    redisComponent.deleteUserActiveMessage(
                            Constants.REDIS_KEY_ACTIVE_MESSAGE + "COLLECT:" + noteId + toUserId);
                } else if (userName3 == null) {
                    // 有两个人收藏，回退到一个人
                    message.setContent(userName2 + "收藏了你的笔记");
                    message.setTime(activeMessageDto.getPostTime());
                    messageMapper.updateById(message);
                    redisComponent.deleteUserActiveMessage(
                            Constants.REDIS_KEY_ACTIVE_MESSAGE + "COLLECT:" + noteId + toUserId);
                } else {
                    // 多人收藏，减少计数
                    message.setContent(userName2 + "," + userName1 + "等" + 
                            (activeMessageDto.getUserCount() - 1) + "位用户收藏了你的笔记");
                    message.setTime(activeMessageDto.getPostTime());
                    messageMapper.updateById(message);
                    redisComponent.deleteUserActiveMessage(
                            Constants.REDIS_KEY_ACTIVE_MESSAGE + "COLLECT:" + noteId + toUserId);
                }
            }
        }
        
        // 删除收藏记录
        userActiveMapper.deleteCollect(userId, noteId);
        
        return "取消收藏成功";
    }

    @Override
    public HashMap<String, Boolean> checkUserActive(String userId,String noteId) {
        HashMap<String,Boolean> resultHash = new HashMap<>();
        //点赞
        if(userActiveMapper.checkLike(userId,noteId)){  
            resultHash.put("like",true);
        }else{
            resultHash.put("like",false);
        }
        //收藏
        if(userActiveMapper.checkCollect(userId,noteId)){
            resultHash.put("collect",true);
        }else{
            resultHash.put("collect",false);
        }
        return resultHash;
    }

    @Override
    @Transactional
    public String addCommentLike(String userId,int commentId) {
        if (userActiveMapper.checkCommentLike(userId, commentId)) {
            throw new IllegalArgumentException("不能重复点赞评论");
        }
        
        // 获取评论信息
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        
        String toUserId = comment.getUserId();
        String noteId = comment.getNoteId();
        
        // 创建点赞记录
        UserActive userActive = new UserActive();
        userActive.setActive("COMMENT_LIKE");
        userActive.setUserId(userId);
        userActive.setCommentId(commentId);
        userActive.setNodeId(noteId);
        userActive.setTime(new Date());
        userActive.setToUserId(toUserId);
        userActiveMapper.insert(userActive);
        
        // 增加评论点赞数
        commentMapper.addLikeCount(commentId);
        
        // 如果是自己的评论，不发送消息
        if (userId.equals(toUserId)) {
            return "评论点赞成功";
        }
        
        // 查询是否有合并消息
        ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(
                Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_LIKE:" + commentId + toUserId);
        String toUserName = userMapper.getUserName(userId);
        
        if (activeMessageDto != null) {
            // 有消息则合并
            activeMessageDto.getUserName().add(toUserName);
            Message message = CopyTools.copy(activeMessageDto, Message.class);
            message.setTime(new Date());
            activeMessageDto.setPostTime(messageMapper.getTimeById(activeMessageDto.getId()));
            activeMessageDto.setUserCount(activeMessageDto.getUserCount() + 1);
            activeMessageDto.setContent(toUserName + "," + 
                    activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 1) + 
                    "等" + activeMessageDto.getUserCount() + "位用户点赞了你的评论");
            messageMapper.updateById(message);
            redisComponent.setUserActiveMessage(
                    Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_LIKE:" + commentId + toUserId, 
                    activeMessageDto);
            redisComponent.setMessage(message);
        } else {
            // 无消息则创建新消息
            Message message = new Message();
            message.setContent(toUserName + "点赞了你的评论");
            message.setUser("system");
            message.setToUser(toUserId);
            message.setType("SYSTEM-USER");
            message.setTime(new Date());
            message.setNoteId(noteId);
            message.setCommentId(commentId);
            messageMapper.insert(message);
            
            activeMessageDto = CopyTools.copy(message, ActiveMessageDto.class);
            List<String> userNameList = new ArrayList<>();
            userNameList.add(toUserName);
            activeMessageDto.setUserName(userNameList);
            activeMessageDto.setUserCount(1);
            redisComponent.setUserActiveMessage(
                    Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_LIKE:" + commentId + toUserId,
                    activeMessageDto);
            redisComponent.setMessage(message);
        }
        
        return "评论点赞成功";
    }

    @Override
    @Transactional
    public String deleteCommentLike(String userId, int commentId) {
        if (!userActiveMapper.checkCommentLike(userId, commentId)) {
            throw new IllegalArgumentException("评论点赞取消失败");
        }
        
        // 获取评论信息
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        
        String toUserId = comment.getUserId();
        
        // 如果不是自己的评论，处理消息
        if (!userId.equals(toUserId)) {
            // 处理合并消息
            ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(
                    Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_LIKE:" + commentId + toUserId);
            
            if (activeMessageDto != null) {
                Message message = CopyTools.copy(activeMessageDto, Message.class);
                String userName1 = null;
                String userName2 = null;
                String userName3 = null;
                
                if (activeMessageDto.getUserName().size() > 0) {
                    userName1 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 1);
                }
                if (activeMessageDto.getUserName().size() > 1) {
                    userName2 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 2);
                }
                if (activeMessageDto.getUserName().size() > 2) {
                    userName3 = activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 3);
                }
                
                if (userName2 == null) {
                    // 只有一条点赞消息，直接删除
                    messageMapper.deleteById(message.getId());
                    redisComponent.deleteUserActiveMessage(
                            Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_LIKE:" + commentId + toUserId);
                } else if (userName3 == null) {
                    // 有两个人点赞，回退到一个人
                    message.setContent(userName2 + "点赞了你的评论");
                    message.setTime(activeMessageDto.getPostTime());
                    messageMapper.updateById(message);
                    redisComponent.deleteUserActiveMessage(
                            Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_LIKE:" + commentId + toUserId);
                } else {
                    // 多人点赞，减少计数
                    message.setContent(userName2 + "," + userName1 + "等" + 
                            (activeMessageDto.getUserCount() - 1) + "位用户点赞了你的评论");
                    message.setTime(activeMessageDto.getPostTime());
                    messageMapper.updateById(message);
                    redisComponent.deleteUserActiveMessage(
                            Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_LIKE:" + commentId + toUserId);
                }
            }
        }
        
        // 删除点赞记录
        userActiveMapper.deleteCommentLike(userId, commentId);
        
        // 减少评论点赞数
        commentMapper.reduceLikeCount(commentId);
        
        return "取消评论点赞成功";
    }
}
