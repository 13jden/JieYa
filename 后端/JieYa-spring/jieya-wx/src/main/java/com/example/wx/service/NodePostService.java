package com.example.wx.service;

import com.example.common.adminDto.NotePostDto;
import com.example.common.pojo.NodeImage;
import com.example.common.pojo.NodePost;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * 添加帖子
     */
    NotePostDto addPostNode(NodePost nodePost,int imageCount);
    
    /**
     * 更新帖子
     */
    NotePostDto updatePostNode(NodePost nodePost, MultipartFile[] newImageFiles, List<NodeImage> imageList);
    
    /**
     * 删除帖子
     */
    boolean deletePostNode(String postId, String userId);
    
    /**
     * 获取单个帖子详情
     */
    NotePostDto getPostNode(String postId);
    
    /**
     * 获取帖子列表（支持分页、分类和关键词筛选）
     */
    Page<NotePostDto> getPostNodeList(Integer page, Integer size, Integer categoryId, String keyword);
    
    /**
     * 获取用户的帖子列表
     */
    Page<NotePostDto> getUserPostNodeList(String userId, Integer page, Integer size);
}
