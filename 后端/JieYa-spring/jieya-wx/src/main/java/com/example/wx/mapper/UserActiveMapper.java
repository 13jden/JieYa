package com.example.wx.mapper;

import com.example.common.pojo.UserActive;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Mapper
public interface UserActiveMapper extends BaseMapper<UserActive> {
    
    /**
     * 获取用户收到的点赞总数
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE to_user_id = #{userId} AND active = 'LIKE'")
    int getUserLikesCount(@Param("userId") String userId);
    
    /**
     * 检查用户是否已点赞某内容
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE user_id = #{userId} AND node_id = #{nodeId} AND active = 'LIKE'")
    int checkUserLike(@Param("userId") String userId, @Param("nodeId") String nodeId);
    
    /**
     * 获取内容的点赞数量
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE node_id = #{nodeId} AND active = 'LIKE'")
    int getObjectLikesCount(@Param("nodeId") String nodeId);

    /**
     * 获取用户收到的点赞列表
     */
    @Select("SELECT * FROM user_active WHERE to_user_id = #{userId} AND active = 'LIKE'")
    List<UserActive> getUserLikesList(@Param("userId") String userId);
    
    /**
     * 获取用户收到的收藏总数
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE to_user_id = #{userId} AND active = 'COLLECT'")
    int getUserCollectsCount(@Param("userId") String userId);
    
    /**
     * 检查用户是否已收藏某内容
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE user_id = #{userId} AND node_id = #{nodeId} AND active = 'COLLECT'")
    int checkUserCollect(@Param("userId") String userId, @Param("nodeId") String nodeId);
    
    /**
     * 获取内容的收藏数量
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE node_id = #{nodeId} AND active = 'COLLECT'")
    int getNodeCollectsCount(@Param("nodeId") String nodeId);

    /**
     * 检查用户是否已点赞某内容
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE user_id = #{userId} AND node_id = #{nodeId} AND active = 'LIKE'")
    boolean checkLike(@Param("userId") String userId, @Param("nodeId") String nodeId);

    /**
     * 检查用户是否已收藏某内容
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE user_id = #{userId} AND node_id = #{nodeId} AND active = 'COLLECT'")
    boolean checkCollect(@Param("userId") String userId, @Param("nodeId") String nodeId);

    /**
     * 删除用户点赞记录
     */
    @Delete("DELETE FROM user_active WHERE user_id = #{userId} AND node_id = #{nodeId} AND active = 'LIKE'")
    void deleteLike(@Param("userId") String userId, @Param("nodeId") String nodeId);
    
    /**
     * 删除用户收藏记录
     */
    @Delete("DELETE FROM user_active WHERE user_id = #{userId} AND node_id = #{nodeId} AND active = 'COLLECT'")
    void deleteCollect(@Param("userId") String userId, @Param("nodeId") String nodeId);

    /**
     * 检查用户是否已点赞某评论
     */
    @Select("SELECT COUNT(*) FROM user_active WHERE user_id = #{userId} AND comment_id = #{commentId} AND active = 'COMMENT_LIKE'")
    boolean checkCommentLike(@Param("userId") String userId, @Param("commentId") int commentId);

    /**
     * 删除用户评论点赞记录
     */
    @Delete("DELETE FROM user_active WHERE user_id = #{userId} AND comment_id = #{commentId} AND active = 'COMMENT_LIKE'")
    void deleteCommentLike(@Param("userId") String userId, @Param("commentId") int commentId);
}
