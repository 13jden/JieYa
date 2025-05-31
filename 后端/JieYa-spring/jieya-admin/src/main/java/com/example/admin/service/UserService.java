package com.example.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.pojo.Message;
import com.example.common.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
public interface UserService extends IService<User> {

    Page<User> getlist(Integer pageNum, Integer pageSize,String status,String username,String userId);


    void unforbidUser(String userId);

    void forbidUser(String userId);

    void deleteUser(String userId);

    void addUser(User user);
}
