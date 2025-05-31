package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.ActiveMessageDto;
import com.example.common.constants.Constants;
import com.example.common.pojo.Message;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.wx.mapper.MessageMapper;
import com.example.wx.mapper.UserMapper;
import com.example.common.WxDto.CommentDto;
import com.example.common.pojo.Comment;
import com.example.common.pojo.User;
import com.example.wx.mapper.CommentMapper;
import com.example.wx.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-04-03
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private MessageMapper messageMapper;
    
    @Value("${host.url}")
    private String hostUrl;
    
//    @Override
//    public Page<CommentDto> getList(int pageNum, int pageSize) {
//        Page<Comment> page = new Page<>(pageNum, pageSize);
//        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.orderByDesc(Comment::getLikeCount, Comment::getTime);
//        Page<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);
//
//        // 处理评论者信息
//        processCommentUserInfo(commentPage.getRecords());
//
//        return commentPage;
//    }
    
    @Override
    public Page<CommentDto> getChildren(String parentId, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId, parentId);
        queryWrapper.orderByDesc(Comment::getLikeCount, Comment::getTime);
        Page<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);
        
        // 处理评论者信息
        Page<CommentDto> commentDtoPage = processCommentUserInfo(commentPage.getRecords());
        
        return commentDtoPage;
    }
    
    @Override
    public Page<CommentDto> getListByNoteId(String noteId, int pageNum, int pageSize) {
        // 查询父评论
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getNoteId, noteId);
        queryWrapper.isNull(Comment::getParentId); // 只查询父评论
        queryWrapper.orderByDesc(Comment::getLikeCount, Comment::getTime);
        Page<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);
        
        // 准备结果页
        Page<CommentDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(commentPage, dtoPage, "records");
        
        // 转换为 CommentDto 并添加子评论数量
        List<CommentDto> dtoList = new ArrayList<>();
        for (Comment comment : commentPage.getRecords()) {
            CommentDto dto = CopyTools.copy(comment,CommentDto.class);
            // 查询子评论数量 - 使用mapper进行统计
            int childCount = commentMapper.countChildrenByParentId(comment.getId());
            dto.setChildrenCount(childCount);
            
            // 设置用户信息
            if (comment.getUserId() != null) {
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    dto.setUserName(user.getNickName());
                    dto.setAvatar(hostUrl+"/images/avatar/"+user.getAvatar());
                }
            }
            
            // 处理被回复者信息
            if (comment.getToUserId() != null) {
                User toUser = userMapper.selectById(comment.getToUserId());
                if (toUser != null) {
                    dto.setReplyName(toUser.getNickName());
                }
            }
            
            // 处理图片URL
            if (dto.getFileUrl() != null && !dto.getFileUrl().startsWith("http")) {
                dto.setFileUrl(hostUrl + "/images/comment/" + dto.getFileUrl());
            }
            
            dtoList.add(dto);
        }
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
    
    @Override
    @Transactional
    public boolean removeCommentWithChildren(String commentId) {
        // 先删除子评论
        LambdaQueryWrapper<Comment> childrenWrapper = new LambdaQueryWrapper<>();
        childrenWrapper.eq(Comment::getParentId, commentId);
        commentMapper.delete(childrenWrapper);
        
        // 再删除父评论
        return commentMapper.deleteById(commentId) > 0;
    }

//    @Override
//    public int addComment(Comment comment) {
//        commentMapper.insert(comment);
//        return comment.getId();
//    }

    @Override
    @Transactional
    public int addComment(Comment comment) {
        // 保存评论
        commentMapper.insert(comment);
        
        // 如果是回复其他评论
        if (comment.getParentId() != null && comment.getToUserId() != null) {
            String userId = comment.getUserId();
            String toUserId = comment.getToUserId();
            String noteId = comment.getNoteId();
            String commentContent = comment.getContent();
            
            // 如果是回复自己的评论，不发送消息
            if (userId.equals(toUserId)) {
                return comment.getId();
            }
            
            // 获取用户名
            String userName = userMapper.getUserName(userId);
            
            // 查询是否有合并消息
            ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(
                    Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_REPLY:" + comment.getParentId() + toUserId);
            
            if (activeMessageDto != null) {
                // 有消息则合并
                activeMessageDto.getUserName().add(userName);
                Message message = CopyTools.copy(activeMessageDto, Message.class);
                message.setTime(new Date());
                activeMessageDto.setPostTime(messageMapper.getTimeById(activeMessageDto.getId()));
                activeMessageDto.setUserCount(activeMessageDto.getUserCount() + 1);
                activeMessageDto.setContent(userName + "," + 
                        activeMessageDto.getUserName().get(activeMessageDto.getUserName().size() - 1) + 
                        "等" + activeMessageDto.getUserCount() + "位用户回复了你的评论");
                messageMapper.updateById(message);
                redisComponent.setUserActiveMessage(
                        Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_REPLY:" + comment.getParentId() + toUserId, 
                        activeMessageDto);
                redisComponent.setMessage(message);
            } else {
                // 无消息则创建新消息
                Message message = new Message();
                message.setContent(userName + "回复了你的评论: " + (commentContent.length() > 20 ? commentContent.substring(0, 20) + "..." : commentContent));
                message.setUser("system");
                message.setToUser(toUserId);
                message.setType("SYSTEM-USER");
                message.setTime(new Date());
                message.setNoteId(noteId);
//                message.setCommentId(comment.getParentId());
                message.setCommentId(comment.getId());
                messageMapper.insert(message);
                
                activeMessageDto = CopyTools.copy(message, ActiveMessageDto.class);
                List<String> userNameList = new ArrayList<>();
                userNameList.add(userName);
                activeMessageDto.setUserName(userNameList);
                activeMessageDto.setUserCount(1);
                redisComponent.setUserActiveMessage(
                        Constants.REDIS_KEY_ACTIVE_MESSAGE + "COMMENT_REPLY:" + comment.getParentId() + toUserId,
                        activeMessageDto);
                redisComponent.setMessage(message);
            }
        }
        
        return comment.getId();
    }

    /**
     * 处理评论中的用户信息并转换为CommentDto
     */
    private Page<CommentDto> processCommentUserInfo(List<Comment> comments) {
        List<CommentDto> commentDtoList = new ArrayList<>();
        
        if (comments != null && !comments.isEmpty()) {
            for (Comment comment : comments) {
                CommentDto dto = CopyTools.copy(comment, CommentDto.class);
                
                // 处理评论者信息
                if (comment.getUserId() != null) {
                    User user = userMapper.selectById(comment.getUserId());
                    if (user != null) {
                        dto.setUserName(user.getNickName());
                        dto.setAvatar(hostUrl + "/images/avatar/" + user.getAvatar());
                    }
                }
                
                // 处理被回复者信息
                if (comment.getToUserId() != null) {
                    User toUser = userMapper.selectById(comment.getToUserId());
                    if (toUser != null) {
                        dto.setReplyName(toUser.getNickName());
                    }
                }
                
                // 处理图片URL
                if (dto.getFileUrl() != null && !dto.getFileUrl().startsWith("http")) {
                    dto.setFileUrl(hostUrl + "/images/comment/" + dto.getFileUrl());
                }
                
                commentDtoList.add(dto);
            }
        }
        
        // 创建并返回分页结果
        Page<CommentDto> dtoPage = new Page<>();
        dtoPage.setRecords(commentDtoList);
        return dtoPage;
    }
}