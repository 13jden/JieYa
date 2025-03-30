package com.example.wx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.pojo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-27
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 获取消息会话列表，每个对话只显示最新一条
     */
    @Select("SELECT * FROM (" +
            "  SELECT m.user, m.to_user as toUser, m.content, m.type, m.time, m.file_url as fileUrl, " +
            "  COUNT(CASE WHEN m.status = 0 AND m.to_user = #{userId} THEN 1 ELSE NULL END) OVER(PARTITION BY LEAST(m.user, m.to_user), GREATEST(m.user, m.to_user)) as notReadCount, " +
            "  ROW_NUMBER() OVER(PARTITION BY LEAST(m.user, m.to_user), GREATEST(m.user, m.to_user) ORDER BY m.time DESC) as rn " +
            "  FROM message m " +
            "  WHERE (m.user = #{userId} OR m.to_user = #{userId}) " +
            "  AND m.type NOT LIKE 'SYSTEM%' AND m.type NOT LIKE 'ORDER%' " +
            ") ranked " +
            "WHERE rn = 1 " +
            "ORDER BY time DESC")
    List<UserMessageDto> selectUserMessagesList(Page<UserMessageDto> page, @Param("userId") String userId);
    
    /**
     * 获取与指定用户的聊天记录
     */
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE ((m.to_user = #{userId} AND m.user = #{targetId}) " +
            "      OR (m.user = #{userId} AND m.to_user = #{targetId})) " +
            "AND m.type NOT LIKE 'SYSTEM%' AND m.type NOT LIKE 'ORDER%' " +
            "ORDER BY m.time DESC")
    Page<Message> selectChatMessages(Page<Message> page, 
            @Param("userId") String userId, 
            @Param("targetId") String targetId);
    
    /**
     * 系统消息
     */
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE m.to_user = #{userId} AND m.type LIKE 'SYSTEM%' " +
            "ORDER BY m.time DESC")
    Page<UserMessageDto> selectSystemMessages(Page<UserMessageDto> page, @Param("userId") String userId);
    
    /**
     * 订单消息
     */
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE m.to_user = #{userId} AND m.type LIKE 'ORDER%' " +
            "ORDER BY m.time DESC")
    Page<UserMessageDto> selectOrderMessages(Page<UserMessageDto> page, @Param("userId") String userId);
    
    /**
     * 获取未读聊天消息数量
     */
    @Select("SELECT COUNT(*) FROM message " +
            "WHERE to_user = #{userId} " +
            "AND type NOT LIKE 'SYSTEM%' AND type NOT LIKE 'ORDER%' " +
            "AND status = 0")
    int getUnreadChatCount(@Param("userId") String userId);
    
    /**
     * 获取未读系统消息数量
     */
    @Select("SELECT COUNT(*) FROM message " +
            "WHERE to_user = #{userId} AND type LIKE 'SYSTEM%' AND status = 0")
    int getUnreadSystemCount(@Param("userId") String userId);
    
    /**
     * 获取未读订单消息数量
     */
    @Select("SELECT COUNT(*) FROM message " +
            "WHERE to_user = #{userId} AND type LIKE 'ORDER%' AND status = 0")
    int getUnreadOrderCount(@Param("userId") String userId);
    
    /**
     * 标记聊天消息为已读
     */
    @Update("UPDATE message SET status = 1 " +
            "WHERE to_user = #{userId} AND user = #{targetId} AND status = 0")
    int markChatMessagesAsRead(@Param("userId") String userId, @Param("targetId") String targetId);
    
    /**
     * 标记系统消息为已读
     */
    @Update("UPDATE message SET status = 1 " +
            "WHERE to_user = #{userId} AND type LIKE 'SYSTEM%' AND status = 0")
    int markSystemMessagesAsRead(@Param("userId") String userId);
    
    /**
     * 标记订单消息为已读
     */
    @Update("UPDATE message SET status = 1 " +
            "WHERE to_user = #{userId} AND type LIKE 'ORDER%' AND status = 0")
    int markOrderMessagesAsRead(@Param("userId") String userId);
}
