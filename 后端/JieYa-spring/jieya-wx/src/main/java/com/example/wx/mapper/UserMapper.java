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
}