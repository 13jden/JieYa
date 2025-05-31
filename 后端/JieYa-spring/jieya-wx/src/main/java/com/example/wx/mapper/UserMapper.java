package com.example.wx.mapper;

import com.example.common.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE nick_name LIKE CONCAT('%', #{nickName}, '%')")
    List<User> searchByNickName(String nickName);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Update("UPDATE user SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp} WHERE email = #{email}")
    void updateLastLogin(User user);

    @Update("UPDATE user SET like_count = like_count + 1 WHERE user_id = #{toUserId}")
    void addLikeCount(String toUserId);

    @Update("UPDATE user SET collect_count = collect_count + 1 WHERE user_id = #{toUserId}")
    void addCollectCount(String toUserId);

    @Update("UPDATE user SET fans_count = fans_count + 1 WHERE user_id = #{toUserId}")
    void addFansCount(String toUserId);

    @Update("UPDATE user SET focus_count = focus_count + 1 WHERE user_id = #{toUserId}")
    void addFocusCount(String toUserId);

    @Update("UPDATE user SET like_count = like_count - 1 WHERE user_id = #{toUserId}")
    void reduceLikeCount(String toUserId);

    @Update("UPDATE user SET collect_count = collect_count - 1 WHERE user_id = #{toUserId}")
    void reduceCollectCount(String toUserId);

    @Update("UPDATE user SET fans_count = fans_count - 1 WHERE user_id = #{toUserId}")
    void reduceFansCount(String toUserId);

    @Update("UPDATE user SET focus_count = focus_count - 1 WHERE user_id = #{toUserId}")
    void reduceFocusCount(String toUserId);

    @Select("SELECT nick_name FROM user WHERE user_id = #{userId}")
    String getUserName(String userId);
}