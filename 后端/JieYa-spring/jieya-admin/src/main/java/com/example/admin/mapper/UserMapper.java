package com.example.admin.mapper;

import com.example.common.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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

}
