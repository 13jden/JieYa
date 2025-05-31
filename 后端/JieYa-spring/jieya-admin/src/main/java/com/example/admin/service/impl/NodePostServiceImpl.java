package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.mapper.NodeImageMapper;
import com.example.admin.mapper.NodeMapper;
import com.example.admin.mapper.NodePostMapper;
import com.example.common.adminDto.NotePostDto;
import com.example.common.pojo.Message;
import com.example.common.pojo.Node;
import com.example.common.pojo.NodeImage;
import com.example.common.pojo.NodePost;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.admin.service.NodePostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Service
public class NodePostServiceImpl extends ServiceImpl<NodePostMapper, NodePost> implements NodePostService {
    
    @Autowired
    private NodePostMapper nodePostMapper;
    
    @Autowired
    private NodeImageMapper nodeImageMapper;
    
    @Autowired
    private NodeMapper nodeMapper;
    
    @Autowired
    private RedisComponent redisComponent;
    
    @Value("${upload.notePath}")
    private String notePath;
    
    @Value("${host.url}")
    private String hostUrl;
    
    @Override
    public Page<NotePostDto> getPendingPostNodeList(Integer page, Integer size) {
        // 创建分页对象
        Page<NodePost> pageParam = new Page<>(page, size);
        
        // 创建查询条件 - 待审核状态为0
        LambdaQueryWrapper<NodePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NodePost::getStatus, 0);
        wrapper.orderByDesc(NodePost::getPostTime);
        
        // 执行分页查询
        Page<NodePost> result = nodePostMapper.selectPage(pageParam, wrapper);
        
        // 转换为DTO对象
        Page<NotePostDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<NotePostDto> records = result.getRecords().stream()
                .map(post -> {
                    NotePostDto dto = CopyTools.copy(post, NotePostDto.class);
                    // 处理封面图片URL
                    if (dto.getCoverImage() != null && !dto.getCoverImage().isEmpty()) {
                        dto.setCoverImage(hostUrl + "/image/note/" + dto.getCoverImage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
    
    @Override
    public Page<NotePostDto> getPostNodeList(Integer page, Integer size, Integer categoryId, String keyword) {
        // 创建分页对象
        Page<NodePost> pageParam = new Page<>(page, size);
        // 创建查询条件
        LambdaQueryWrapper<NodePost> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(NodePost::getCategory, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(NodePost::getTitle, keyword).or().like(NodePost::getContent, keyword);
        }
        wrapper.orderByDesc(NodePost::getPostTime);
        
        // 执行分页查询
        Page<NodePost> result = nodePostMapper.selectPage(pageParam, wrapper);
        
        // 转换为DTO对象
        Page<NotePostDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<NotePostDto> records = result.getRecords().stream()
                .map(post -> {
                    NotePostDto dto = CopyTools.copy(post, NotePostDto.class);
                    // 处理封面图片URL
                    if (dto.getCoverImage() != null && !dto.getCoverImage().isEmpty()) {
                        dto.setCoverImage(hostUrl + "/image/note/" + dto.getCoverImage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
    
    @Override
    public Page<NotePostDto> searchPostNodeByTitle(String title, Integer page, Integer size) {
        // 创建分页对象
        Page<NodePost> pageParam = new Page<>(page, size);
        
        // 创建查询条件
        LambdaQueryWrapper<NodePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(NodePost::getTitle, title);
        wrapper.orderByDesc(NodePost::getPostTime);
        
        // 执行分页查询
        Page<NodePost> result = nodePostMapper.selectPage(pageParam, wrapper);
        
        // 转换为DTO对象
        Page<NotePostDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<NotePostDto> records = result.getRecords().stream()
                .map(post -> {
                    NotePostDto dto = CopyTools.copy(post, NotePostDto.class);
                    // 处理封面图片URL
                    if (dto.getCoverImage() != null && !dto.getCoverImage().isEmpty()) {
                        dto.setCoverImage(hostUrl + "/image/note/" + dto.getCoverImage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
    
    @Override
    public NotePostDto getPostNodeDetail(String postId) {
        // 查询帖子信息
        NodePost post = nodePostMapper.selectById(postId);
        if (post == null) {
            return null;
        }
        
        // 复制到DTO
        NotePostDto notePostDto = CopyTools.copy(post, NotePostDto.class);
        
        // 查询相关图片
        LambdaQueryWrapper<NodeImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NodeImage::getNodeId, postId);
        List<NodeImage> images = nodeImageMapper.selectList(wrapper);
        
        // 处理图片URL
        for (NodeImage image : images) {
            image.setImagePath(hostUrl + "/image/note/" + image.getImagePath());
        }
        
        // 设置图片列表
        notePostDto.setImages(images);
        
        // 处理封面图片URL
        if (notePostDto.getCoverImage() != null && !notePostDto.getCoverImage().isEmpty()) {
            notePostDto.setCoverImage(hostUrl + "/image/note/" + notePostDto.getCoverImage());
        }
        
        return notePostDto;
    }
    
    @Override
    @Transactional
    public boolean approvePostNode(String postId) {
        // 查询帖子
        NodePost post = nodePostMapper.selectById(postId);
        if (post == null) {
            return false;
        }
        
        try {
            // 1. 将审核状态更新为已通过(1)
            post.setStatus(1);
            post.setPostTime(new Date());
            nodePostMapper.updateById(post);
            Node oldNode =  nodeMapper.selectById(postId);

            // 2. 复制到正式表
            Node node = CopyTools.copy(post, Node.class);
            if(oldNode!=null){
                node.setCollectCount(oldNode.getCollectCount());
                node.setLikeCount(oldNode.getLikeCount());
                node.setRecommendCount(oldNode.getRecommendCount());
                node.setViewCount(oldNode.getViewCount());
            }
            else {
                node.setCollectCount(0);
                node.setLikeCount(0);
                node.setRecommendCount(0);
                node.setViewCount(0);
            }
            nodeMapper.insert(node);
            
            // 3. 给用户发送审核通过的消息
            Message message = new Message();
            message.setTime(new Date());
            message.setContent("您的笔记《" + post.getTitle() + "》已审核通过");
            message.setUser("admin");
            message.setToUser(post.getUserId());
            message.setType("SYSTEM-USER");
            redisComponent.setMessage(message);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("审核通过笔记时发生错误", e);
        }
    }
    
    @Override
    @Transactional
    public boolean rejectPostNode(String postId, String reason) {
        // 查询帖子
        NodePost post = nodePostMapper.selectById(postId);
        if (post == null) {
            return false;
        }
        
        try {
            // 1. 将审核状态更新为已拒绝(2)
            post.setStatus(2);
            nodePostMapper.updateById(post);
            
            // 2. 给用户发送审核拒绝的消息
            Message message = new Message();
            message.setTime(new Date());
            message.setContent("您的笔记《" + post.getTitle() + "》审核未通过，原因：" + reason);
            message.setUser("admin");
            message.setToUser(post.getUserId());
            message.setType("SYSTEM-USER");
            redisComponent.setMessage(message);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("驳回笔记时发生错误", e);
        }
    }
    
    @Override
    @Transactional
    public boolean deletePostNode(String postId) {
        // 查询帖子
        NodePost post = nodePostMapper.selectById(postId);
        if (post == null) {
            return false;
        }
        
        try {
            // 1. 查询所有相关图片
            LambdaQueryWrapper<NodeImage> imageWrapper = new LambdaQueryWrapper<>();
            imageWrapper.eq(NodeImage::getId, postId);
            List<NodeImage> images = nodeImageMapper.selectList(imageWrapper);
            
            // 2. 删除相关图片文件
            for (NodeImage image : images) {
                String filePath = notePath + image.getImagePath();
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            
            // 3. 删除图片记录
            nodeImageMapper.delete(imageWrapper);
            
            // 4. 如果已审核通过，还需要从正式表中删除
            if (post.getStatus() == 1) {
                nodeMapper.deleteById(postId);
            }
            
            // 5. 删除帖子
            nodePostMapper.deleteById(postId);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除笔记时发生错误", e);
        }
    }
    
    @Override
    public Page<NotePostDto> getUserPostNodeList(String userId, Integer page, Integer size) {
        // 创建分页对象
        Page<NodePost> pageParam = new Page<>(page, size);
        
        // 创建查询条件
        LambdaQueryWrapper<NodePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NodePost::getUserId, userId);
        wrapper.orderByDesc(NodePost::getPostTime);
        
        // 执行分页查询
        Page<NodePost> result = nodePostMapper.selectPage(pageParam, wrapper);
        
        // 转换为DTO对象
        Page<NotePostDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<NotePostDto> records = result.getRecords().stream()
                .map(post -> {
                    NotePostDto dto = CopyTools.copy(post, NotePostDto.class);
                    // 处理封面图片URL
                    if (dto.getCoverImage() != null && !dto.getCoverImage().isEmpty()) {
                        dto.setCoverImage(hostUrl + "/image/note/" + dto.getCoverImage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
    
    @Override
    public boolean updatePostNodeCategory(String postId, Integer categoryId) {
        // 查询帖子
        NodePost post = nodePostMapper.selectById(postId);
        if (post == null) {
            return false;
        }
        
        try {
            // 更新分类
            post.setCategory(categoryId);
            nodePostMapper.updateById(post);
            
            // 如果已审核通过，同时更新正式表
            if (post.getStatus() == 1) {
                Node node = nodeMapper.selectById(postId);
                if (node != null) {
                    node.setCategory(categoryId);
                    nodeMapper.updateById(node);
                }
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新笔记分类时发生错误", e);
        }
    }
}
