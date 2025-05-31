package com.example.wx.mapper;

import com.example.common.pojo.Focus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Mapper

public interface FocusMapper extends BaseMapper<Focus> {
    
    /**
     * 获取用户粉丝数量
     */
    @Select("SELECT COUNT(*) FROM focus WHERE focus_id = #{userId}")
    int getUserFansCount(@Param("userId") String userId);
    
    /**
     * 获取用户关注数量
     */
    @Select("SELECT COUNT(*) FROM focus WHERE user_id = #{userId}")
    int getUserFocusCount(@Param("userId") String userId);
    
    /**
     * 检查用户是否已关注
     */
    @Select("SELECT COUNT(*) FROM focus WHERE user_id = #{userId} AND focus_id = #{toUserId}")
    int checkUserFocus(@Param("userId") String userId, @Param("toUserId") String toUserId);

    /**
     * 检查是否已关注某用户
     */
    @Select("SELECT COUNT(*) FROM focus WHERE user_id = #{userId} AND focus_id = #{focusUserId}")
    boolean checkFocus(@Param("userId") String userId, @Param("focusUserId") String focusUserId);

    /**
     * 删除关注记录
     */
    @Delete("DELETE FROM focus WHERE user_id = #{userId} AND focus_id = #{focusUserId}")
    void deleteFocus(@Param("userId") String userId, @Param("focusUserId") String focusUserId);
}
