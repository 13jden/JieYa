package com.example.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.pojo.User;
import com.example.admin.mapper.UserMapper;
import com.example.admin.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

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

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisComponent redisComponent;

    @Override
    public Page<User> getlist(Integer pageNum, Integer pageSize, String status, String username, String userId) {
        // 创建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);
        
        // 创建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 可以在这里根据需要添加查询条件
        if("1".equals(status)){
            queryWrapper.eq(User::getStatus, true);
        }
        else if("0".equals(status)){
            queryWrapper.eq(User::getStatus, false);
        }
        
        if(username != null && !username.trim().isEmpty()){
            queryWrapper.like(User::getNickName, username);
        }
        
        if(userId != null && !userId.trim().isEmpty()){
            queryWrapper.eq(User::getUserId, userId);
        }
        
        // 添加排序条件
        queryWrapper.orderByDesc(User::getJoinTime);
        
        // 3. 添加调试日志
        System.out.println("查询条件: " + queryWrapper.getTargetSql());
        
        // 执行分页查询
        return page(page, queryWrapper);
    }

    @Override
    public void unforbidUser(String userId) {
        User user = getById(userId);
        if (user != null) {
            user.setStatus(true); // 1表示正常状态
            updateById(user);
            // 从JWT黑名单中移除用户
            jwtUtil.invalidateDeleteUserTokens(userId);
        }
    }

    @Override
    public void forbidUser(String userId) {
        User user = getById(userId);
        if (user != null) {
            user.setStatus(false); // 0表示禁用状态
            updateById(user);
            // 将用户添加到JWT黑名单
            jwtUtil.invalidateUserTokens(userId);
        }
    }

    @Override
    public void deleteUser(String userId) {
        // 先将用户禁用
        forbidUser(userId);
        // 删除用户及相关数据
        removeById(userId);
        // 清除Redis中的用户相关数据
        jwtUtil.invalidateDeleteUserTokens(userId);
    }

    @Override
    public void addUser(User user) {
        // 检查用户是否已存在
        User existUser = userMapper.selectById(user.getUserId());
        if (existUser != null) {
            throw new RuntimeException("用户已存在");
        }
        // 创建新用户
        user.setStatus(true); // 默认为启用状态
        user.setJoinTime(new Date());
        
        // 保存用户
        save(user);
    }
}
