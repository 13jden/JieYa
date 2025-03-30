package com.example.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 0：女，1：男，2：未知
     */
    private Boolean sex;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 学校
     */
    private String school;

    /**
     * 简介
     */
    private String personIntruduction;

    /**
     * 注册时间
     */
    private Date joinTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录ip
     */
    private String lastLoginIp;

    /**
     * 账号状态：0：禁用，1：正常
     */
    private Boolean status;

    /**
     * 主题 
     */
    private Boolean theme;

    /**
     * 空间公告
     */
    private String noticeInfo;

    private String avatar;


}
