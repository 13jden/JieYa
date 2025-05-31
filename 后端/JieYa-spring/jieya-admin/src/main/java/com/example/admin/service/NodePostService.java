package com.example.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.adminDto.NotePostDto;
import com.example.common.pojo.NodePost;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
public interface NodePostService extends IService<NodePost> {

    /**
     * 获取待审核的笔记列表
     */
    Page<NotePostDto> getPendingPostNodeList(Integer page, Integer size);
    
    /**
     * 获取所有笔记列表
     */
    Page<NotePostDto> getPostNodeList(Integer page, Integer size, Integer categoryId, String keyword);
    
    /**
     * 按标题搜索笔记
     */
    Page<NotePostDto> searchPostNodeByTitle(String title, Integer page, Integer size);
    
    /**
     * 获取笔记详情
     */
    NotePostDto getPostNodeDetail(String postId);
    
    /**
     * 审核通过笔记
     */
    boolean approvePostNode(String postId);
    
    /**
     * 驳回笔记
     */
    boolean rejectPostNode(String postId, String reason);
    
    /**
     * 删除笔记
     */
    boolean deletePostNode(String postId);
    
    /**
     * 获取用户的笔记列表
     */
    Page<NotePostDto> getUserPostNodeList(String userId, Integer page, Integer size);
    
    /**
     * 更新笔记分类
     */
    boolean updatePostNodeCategory(String postId, Integer categoryId);
}
