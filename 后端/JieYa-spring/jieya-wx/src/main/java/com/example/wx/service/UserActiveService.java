package com.example.wx.service;

import com.example.common.common.Result;
import com.example.common.pojo.UserActive;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
public interface UserActiveService extends IService<UserActive> {



    Object getCollectList(String userId);

    String addLike(String userId, String noteId,String toUserId);

    String deleteLike(String userId, String noteId,String toUserId);

    String addCollect(String userId, String noteId);

    String deleteCollect(String userId, String noteId);

    HashMap<String,Boolean> checkUserActive(String userId, String noteId);

    String addCommentLike(String userId,int commentId);

    String deleteCommentLike(String userId,int commentId);
}
