package com.example.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.pojo.Comment;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-04-03
 */
public interface CommentService extends IService<Comment> {

    Page<Comment> getList(int pageNum, int pageSize);
}
