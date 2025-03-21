package com.example.admin.service.impl;

import com.example.common.pojo.User;
import com.example.admin.mapper.UserMapper;
import com.example.admin.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
