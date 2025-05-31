package com.example.wx.service;

import com.example.common.pojo.Focus;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
public interface FocusService extends IService<Focus> {

    Object getFocusList(String userId);

    Object deleteFocus(String userId, String focusUserId);

    Object addFocus(String userId, String focusUserId);

    Object getFansList(String userId);
}
