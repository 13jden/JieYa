package com.example.wx.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.CommentDto;
import com.example.common.pojo.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-04-03
 */
public interface CommentService extends IService<Comment> {

    /**
     * 获取评论列表（分页）
     */
//    Page<Comment> getList(int pageNum, int pageSize);
    
    /**
     * 获取子评论列表（分页）
     */
    Page<CommentDto> getChildren(String parentId, int pageNum, int pageSize);
    
    /**
     * 根据笔记ID获取父评论列表（分页），并返回子评论数量
     */
    Page<CommentDto> getListByNoteId(String noteId, int pageNum, int pageSize);
    
    /**
     * 删除评论及其子评论
     */
    boolean removeCommentWithChildren(String commentId);

    int addComment (Comment Comment);
}
