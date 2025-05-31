package com.example.wx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.CommentDto;
import com.example.common.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-04-03
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 统计父评论下的子评论数量
     */
    @Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{parentId}")
    int countChildrenByParentId(@Param("parentId") Integer parentId);
    
    /**
     * 根据笔记ID统计评论数量
     */
    @Select("SELECT COUNT(*) FROM comment WHERE note_id = #{noteId}")
    int countCommentsByNoteId(@Param("noteId") String noteId);

    /**
     * 增加评论的点赞数
     */
    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int addLikeCount(@Param("commentId") int commentId);

    /**
     * 减少评论的点赞数
     */
    @Update("UPDATE comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{commentId}")
    int reduceLikeCount(@Param("commentId") int commentId);
}
