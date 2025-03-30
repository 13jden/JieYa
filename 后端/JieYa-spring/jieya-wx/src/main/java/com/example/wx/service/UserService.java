package com.example.wx.service;

import com.example.common.WxDto.TokenUserInfoDto;
import com.example.common.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
public interface UserService extends IService<User> {

    List<User> searchByNickName(String nickName);

    String register(String email, String nickName, String password);

    TokenUserInfoDto login(String email, String password, String ip);

    boolean update(User updateUser);
}
