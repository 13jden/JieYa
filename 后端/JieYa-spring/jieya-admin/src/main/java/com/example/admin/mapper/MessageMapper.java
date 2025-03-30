package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.adminDto.AdminMessageDto;
import com.example.common.pojo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    // 从消息表查询，确保每对用户只返回一条最新消息，并只统计发给admin的未读消息
    @Select("SELECT * FROM (" +
            "  SELECT m.user, m.to_user as toUser, m.content, m.type, m.time, " +
            "  COUNT(CASE WHEN m.status = 0 AND m.to_user = 'admin' THEN 1 ELSE NULL END) OVER(PARTITION BY LEAST(m.user, m.to_user), GREATEST(m.user, m.to_user)) as notReadCount, " +
            "  ROW_NUMBER() OVER(PARTITION BY LEAST(m.user, m.to_user), GREATEST(m.user, m.to_user) ORDER BY m.time DESC) as rn " +
            "  FROM message m " +
            "  WHERE m.type = 'ADMIN-USER' " +
            ") ranked " +
            "WHERE rn = 1 " +
            "ORDER BY time DESC")
    List<AdminMessageDto> selectUserMessageList(Page<AdminMessageDto> page);
    
    // 根据用户ID查询最近一条消息内容
    @Select("SELECT content FROM message " +
            "WHERE (to_user = #{toUser} OR user = #{toUser}) " +
            "AND type = 'ADMIN-USER' " +
            "ORDER BY time DESC LIMIT 1")
    String getLastMessageContent(@Param("toUser") String toUser);
    
    // 获取用户的聊天消息
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE ((m.to_user = #{toUser} AND m.user = #{otherUser}) " +
            "      OR (m.user = #{toUser} AND m.to_user = #{otherUser})) " +
            "AND m.type = 'ADMIN-USER' " +
            "ORDER BY m.time DESC")
    Page<Message> selectUserMessageItemList(Page<Message> page, 
            @Param("toUser") String toUser, 
            @Param("otherUser") String otherUser);
    
    // 系统消息
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE m.type = 'SYSTEM' " +
            "ORDER BY m.time DESC")
    Page<AdminMessageDto> selectSystemMessage(Page<AdminMessageDto> page);

    // 订单消息
    @Select("SELECT m.* " +
            "FROM message m " +
            "WHERE m.type = 'ORDER' " +
            "ORDER BY m.time DESC")
    Page<AdminMessageDto> selectOrderMessage(Page<AdminMessageDto> page);

    @Select("SELECT COUNT(*) FROM message " +
            "WHERE type NOT LIKE 'ORDER%' AND type NOT LIKE 'SYSTEM%' AND status = 0")
    Integer selectUserMessageCount();
    
    @Select("SELECT COUNT(*) FROM message WHERE type LIKE 'ORDER%' AND status = 0")
    Integer selectOrderMessageCount();
    
    @Select("SELECT COUNT(*) FROM message WHERE type LIKE 'SYSTEM%' AND status = 0")
    Integer selectSystemMessageCount();
    
    @Delete("DELETE FROM message WHERE to_user = #{toUser} OR user = #{toUser}")
    int deleteUserMessage(@Param("toUser") String toUser);
    
    // 修正已读操作接口，使用status字段
    @Update("UPDATE message SET status = 1 WHERE type LIKE 'ORDER%'")
    int markOrderMessagesAsRead();
    
    @Update("UPDATE message SET status = 1 WHERE type LIKE 'SYSTEM%'")
    int markSystemMessagesAsRead();
    
    @Update("UPDATE message SET status = 1 WHERE to_user = #{toUser} OR user = #{toUser}")
    int markUserMessagesAsRead(@Param("toUser") String toUser);
    
    @Update("UPDATE message SET status = 1")
    int markAllMessagesAsRead();
}