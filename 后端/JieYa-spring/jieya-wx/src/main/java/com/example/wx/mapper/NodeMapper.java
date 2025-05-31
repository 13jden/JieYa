package com.example.wx.mapper;

import com.example.common.pojo.Node;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Mapper

public interface NodeMapper extends BaseMapper<Node> {

    /**
     * 增加笔记的点赞数
     */
    @Update("UPDATE node SET like_count = like_count + 1 WHERE id = #{noteId}")
    int addLikeCount(@Param("noteId") String noteId);

    /**
     * 减少笔记的点赞数
     */
    @Update("UPDATE node SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{noteId}")
    int reduceLikeCount(@Param("noteId") String noteId);

    @Update("UPDATE node SET collect_count = collect_count + 1 WHERE id = #{noteId}")
    int addCollectCount(@Param("noteId") String noteId);
}
