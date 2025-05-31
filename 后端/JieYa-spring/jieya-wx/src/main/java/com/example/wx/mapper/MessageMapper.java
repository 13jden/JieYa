package com.example.wx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.UserMessageDto;
import com.example.common.pojo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
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
    @Select("""
    SELECT * FROM (
      SELECT 
        m.id, m.user, m.to_user as toUser, m.content, m.type, m.time, m.file_url as fileUrl, 
        COUNT(CASE WHEN m.status = 0 AND m.to_user = #{userId} THEN 1 ELSE NULL END) 
            OVER(PARTITION BY LEAST(m.user, m.to_user), GREATEST(m.user, m.to_user)) as notReadCount,
        ROW_NUMBER() OVER(
            PARTITION BY LEAST(m.user, m.to_user), GREATEST(m.user, m.to_user) 
            ORDER BY m.time DESC
        ) as rn
      FROM message m
      WHERE (m.user = #{userId} OR m.to_user = #{userId})
      AND m.type = 'ADMIN-USER'
    ) ranked
    WHERE rn = 1
    ORDER BY time DESC
""")
    List<UserMessageDto> selectUserMessagesList(Page<UserMessageDto> page, @Param("userId") String userId);
    
    /**
     * 获取与指定用户的聊天记录
     */
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE ((m.to_user = #{userId} AND m.user = #{targetId}) " +
            "      OR (m.user = #{userId} AND m.to_user = #{targetId})) " +
            "AND m.type NOT LIKE 'SYSTEM-USER%' AND m.type NOT LIKE 'ORDER-USER%' " +
            "ORDER BY m.time DESC")
    Page<Message> selectChatMessages(Page<Message> page, 
            @Param("userId") String userId, 
            @Param("targetId") String targetId);
    
    /**
     * 系统消息
     */
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE m.to_user = #{userId} AND m.type LIKE 'SYSTEM-USER%' " +
            "ORDER BY m.time DESC")
    Page<UserMessageDto> selectSystemMessages(Page<UserMessageDto> page, @Param("userId") String userId);
    
    /**
     * 订单消息
     */
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE m.to_user = #{userId} AND m.type LIKE 'ORDER-USER%' " +
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
            "WHERE to_user = #{userId} AND type LIKE 'SYSTEM-USER%' AND status = 0")
    int getUnreadSystemCount(@Param("userId") String userId);
    
    /**
     * 获取未读订单消息数量
     */
    @Select("SELECT COUNT(*) FROM message " +
            "WHERE to_user = #{userId} AND type LIKE 'ORDER-USER%' AND status = 0")
    int getUnreadOrderCount(@Param("userId") String userId);


    /**
     * 获取未读消息数量
     */
    @Select("SELECT COUNT(*) FROM message " +
            "WHERE to_user = #{userId} AND status = 0")
    int getUnreadAllCount(@Param("userId") String userId);

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
            "WHERE to_user = #{userId} AND type LIKE 'SYSTEM-USER%' AND status = 0")
    int markSystemMessagesAsRead(@Param("userId") String userId);
    
    /**
     * 标记订单消息为已读
     */
    @Update("UPDATE message SET status = 1 " +
            "WHERE to_user = #{userId} AND type LIKE 'ORDER-USER%' AND status = 0")
    int markOrderMessagesAsRead(@Param("userId") String userId);
    
    /**
     * 获取最新系统消息
     */
    @Select("SELECT m.user, m.to_user as toUser, m.content, m.type, m.time, m.FileUrl as fileUrl, " +
            "COUNT(CASE WHEN m2.status = 0 THEN 1 ELSE NULL END) as notReadCount " +
            "FROM message m " +
            "LEFT JOIN message m2 ON m2.type LIKE 'SYSTEM-USER%' AND m2.to_user = #{userId} " +
            "WHERE m.to_user = #{userId} AND m.type LIKE 'SYSTEM-USER%' " +
            "GROUP BY m.user, m.to_user, m.content, m.type, m.time, m.FileUrl " +
            "ORDER BY m.time DESC " +
            "LIMIT 1")
    List<UserMessageDto> selectLatestSystemMessages(@Param("userId") String userId);
    
    /**
     * 获取最新订单消息
     */
    @Select("SELECT m.user, m.to_user as toUser, m.content, m.type, m.time, m.FileUrl as fileUrl, " +
            "COUNT(CASE WHEN m2.status = 0 THEN 1 ELSE NULL END) as notReadCount " +
            "FROM message m " +
            "LEFT JOIN message m2 ON m2.type LIKE 'ORDER-USER%' AND m2.to_user = #{userId} " +
            "WHERE m.to_user = #{userId} AND m.type LIKE 'ORDER-USER%' " +
            "GROUP BY m.user, m.to_user, m.content, m.type, m.time, m.FileUrl " +
            "ORDER BY m.time DESC " +
            "LIMIT 1")
    List<UserMessageDto> selectLatestOrderMessages(@Param("userId") String userId);
    
    /**
     * 获取最新用户消息（每个对话只显示最新一条）
     */
    @Select("SELECT m.user, m.to_user as toUser, m.content, m.type, m.time, m.FileUrl as fileUrl, " +
            "COUNT(CASE WHEN m2.status = 0 AND m2.to_user = #{userId} THEN 1 ELSE NULL END) as notReadCount " +
            "FROM message m " +
            "LEFT JOIN message m2 ON ((m2.user = m.user AND m2.to_user = m.to_user) OR (m2.user = m.to_user AND m2.to_user = m.user)) " +
            "WHERE (m.user = #{userId} OR m.to_user = #{userId}) " +
            "AND m.type = 'USER-USER' " +
            "GROUP BY m.user, m.to_user, m.content, m.type, m.time, m.FileUrl " +
            "ORDER BY m.time DESC")
    List<UserMessageDto> selectLatestUserMessages(@Param("userId") String userId);
    
    /**
     * 获取最新互动消息
     */
    @Select("SELECT m.user, m.to_user as toUser, m.content, m.type, m.time, m.FileUrl as fileUrl, " +
            "COUNT(CASE WHEN m2.status = 0 AND m2.to_user = #{userId} THEN 1 ELSE NULL END) as notReadCount " +
            "FROM message m " +
            "LEFT JOIN message m2 ON m2.type = 'USER-USER' AND m2.to_user = #{userId} " +
            "WHERE m.to_user = #{userId} " +
            "AND m.type IN ('COMMENT', 'LIKE', 'FOLLOW') " +
            "GROUP BY m.id, m.user, m.to_user, m.content, m.type, m.time, m.FileUrl " +
            "ORDER BY m.time DESC")
    List<UserMessageDto> selectLatestInteractionMessages(@Param("userId") String userId);

    @Select("SELECT time FROM message WHERE id = #{id}")
    Date getTimeById(Long id);

    @Select("""
    SELECT m.*
    FROM message m
    INNER JOIN (
        SELECT 
            CASE 
                WHEN user < to_user THEN CONCAT(user, '_', to_user)
                ELSE CONCAT(to_user, '_', user)
            END AS group_id,
            MAX(id) AS max_id
        FROM message
        WHERE user = #{currentUserId} OR to_user = #{currentUserId}
        GROUP BY group_id
    ) latest ON m.id = latest.max_id
    ORDER BY m.time DESC
""")
    IPage<Message> getUserMessageList(@Param("page") IPage<Message> page, @Param("currentUserId") String currentUserId);

    /**
     * 查询指定用户发来的未读消息数量
     */
    @Select("SELECT COUNT(*) FROM message WHERE to_user = #{currentUserId} AND user = #{fromUserId} AND status = 0")
    int countUnreadMessagesFromUser(@Param("currentUserId") String currentUserId, @Param("fromUserId") String fromUserId);

    /**
     * 查询指定类型的未读消息数量
     */
    @Select("SELECT COUNT(*) FROM message WHERE to_user = #{currentUserId} AND user = #{userType} AND status = 0")
    int countUnreadMessages(@Param("currentUserId") String currentUserId, @Param("userType") String userType);

}
