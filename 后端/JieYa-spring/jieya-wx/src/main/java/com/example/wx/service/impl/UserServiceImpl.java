package com.example.wx.service.impl;

import com.example.common.constants.Constants;
import com.example.common.pojo.User;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.common.utils.JwtUtil;
import com.example.common.utils.StringTools;
import com.example.common.WxDto.TokenUserInfoDto;
import com.example.wx.mapper.UserMapper;
import com.example.wx.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    private RedisComponent redisComponent;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${host.url}")
    private String hostUrl;

    @Override
    public List<User> searchByNickName(String nickName) {
        return userMapper.searchByNickName(nickName);  // 调用 Mapper 中的查询方法
    }

    @Override
    public String register(String email, String nickName, String password){
        if(userMapper.findByEmail(email)!=null){
            return "邮箱重复";
        }
        User user = new User();
        user.setEmail(email);
        user.setNickName(nickName);
        user.setJoinTime(new Date());
        String userId = StringTools.getRandomBumber(Constants.LENGTH_10);
        user.setUserId(userId);
        user.setPassword(StringTools.encodeByMd5(password));

        if(userMapper.insert(user)>0)
            return "注册成功";

        return "注册失败";
    }

    @Override
    public TokenUserInfoDto login(String email, String password, String ip) {
        User user = userMapper.findByEmail(email);
        if (user == null || !user.getPassword().equals(StringTools.encodeByMd5(password))) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        // 检查用户状态
        if (!user.getStatus()) {
            throw new IllegalArgumentException("账号已被封禁");
        }
        
        // 更新用户登录信息
        user.setLastLoginTime(new Date());
        user.setLastLoginIp(ip);
        // 更新数据库中的登录时间和IP
        userMapper.updateLastLogin(user);
        
        // 创建TokenUserInfoDto对象
        TokenUserInfoDto tokenUserInfoDto = CopyTools.copy(user, TokenUserInfoDto.class);
        tokenUserInfoDto.setAvatar(hostUrl + "/images/avatar/" + tokenUserInfoDto.getAvatar());
        tokenUserInfoDto.setSchool(user.getSchool());
        tokenUserInfoDto.setPersonIntruduction(user.getPersonIntruduction());
        tokenUserInfoDto.setSex(user.getSex());
        tokenUserInfoDto.setBirthday(user.getBirthday());   
        try {
            // 生成JWT token
            String token = JwtUtil.generateToken(user.getUserId().toString());
            
            // 设置token和过期时间
            tokenUserInfoDto.setToken(token);
            tokenUserInfoDto.setExpireAt(System.currentTimeMillis() + Constants.REDIS_KEY_EXPIRES_ONE_DAY * 7);
            
            // 保存到Redis
            redisComponent.saveTokenInfo(token, tokenUserInfoDto);
            
            return tokenUserInfoDto;
        } catch (Exception e) {
            throw new RuntimeException("生成token失败", e);
        }
    }

    @Override
    public boolean update(User updateUser) {
        return userMapper.updateById(updateUser)>0;
    }
}
