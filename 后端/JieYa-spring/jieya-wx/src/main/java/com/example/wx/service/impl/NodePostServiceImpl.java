package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.adminDto.NotePostDto;
import com.example.common.constants.Constants;
import com.example.common.pojo.Message;
import com.example.common.pojo.NodeImage;
import com.example.common.pojo.NodePost;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.common.utils.StringTools;
import com.example.wx.mapper.MessageMapper;
import com.example.wx.mapper.NodeImageMapper;
import com.example.wx.mapper.NodeMapper;
import com.example.wx.mapper.NodePostMapper;
import com.example.wx.service.NodePostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
    private MessageMapper messageMapper;

    @Autowired
    private RedisComponent redisComponent;

    @Value("${upload.notePath}")
    private String notePath;

    @Value("${host.url}")
    private String hostUrl;


    @Autowired
    private NodeMapper nodeMapper;

    
    @Transactional
    @Override
    public NotePostDto addPostNode(NodePost nodePost,int imageCount) {
        try {
            nodePost.setStatus(0);
            List<NodeImage> nodeImageList = new ArrayList<>();
            for(int i=1;i<=imageCount;i++){
                NodeImage nodeImage = redisComponent.getTempNoteImage(nodePost.getId(),i);
                if(nodeImage==null){
                    throw new RuntimeException("上传错误，请重新上传");
                }
                nodeImageList.add(nodeImage);
            }

            // 创建基于日期的文件夹路径
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateFolder = sdf.format(new Date());
            String datePath = notePath + dateFolder + "/";

            // 确保目录存在
            File dateDir = new File(datePath);
            if (!dateDir.exists()) {
                dateDir.mkdirs();
            }

            // 存储创建的正式NodeImage对象列表
            List<NodeImage> createdImages = new ArrayList<>();
            boolean isCoverSet = false;

            //创建nodepost保证id有效
            nodePostMapper.insert(nodePost);

            // 处理每个临时图片
            for (NodeImage tempImage : nodeImageList) {
                // 获取临时文件路径
                String tempFilePath = tempImage.getImagePath();
                File tempFile = new File(tempFilePath);
                
                if (!tempFile.exists()) {
                    throw new RuntimeException("临时文件不存在：" + tempFilePath);
                }
                
                // 生成唯一文件名
                String originalFilename = tempFile.getName();
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                String fileName = UUID.randomUUID().toString() + suffix;
                String newFilePath = datePath + fileName;
                String imageUrl = dateFolder + "/" + fileName;

                // 移动文件到正式目录
                File newFile = new File(newFilePath);
                Files.copy(tempFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // 设置封面图片（使用第一张图片作为封面）
                if (!isCoverSet) {
                    nodePost.setCoverImage(imageUrl);
                    nodePostMapper.updateById(nodePost);
                    isCoverSet = true;
                }

                // 创建正式图片记录并保存到数据库
                NodeImage newImage = new NodeImage();
                newImage.setNodeId(nodePost.getId());
                newImage.setSort(tempImage.getSort());
                newImage.setImagePath(imageUrl);
                nodeImageMapper.insert(newImage);
                createdImages.add(newImage);
                // 删除临时文件
                tempFile.delete();
                // 删除Redis中的临时记录
                redisComponent.deleteTempNoteImage(nodePost.getId(), tempImage.getSort());
            }

            // 3. 发送系统消息
            Message message = new Message();
            message.setTime(new Date());
            message.setContent("用户：" + nodePost.getUserId() + "上传了笔记，请审核");
            message.setUser("system");
            message.setToUser("admin");
            message.setType("SYSTEM-ADMIN");
            messageMapper.insert(message);
            redisComponent.setMessage(message);

            NotePostDto notePostDto = CopyTools.copy(nodePost, NotePostDto.class);
            notePostDto.setImages(createdImages);
            return notePostDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("保存笔记失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public NotePostDto updatePostNode(NodePost nodePost, MultipartFile[] newImageFiles, List<NodeImage> imageList) {
        // 查询原帖子确认存在
        NodePost existingPost = nodePostMapper.selectById(nodePost.getId());
        if (existingPost == null) {
            return null;
        }
        
        // 更新基本信息
        existingPost.setTitle(nodePost.getTitle());
        existingPost.setContent(nodePost.getContent());
        existingPost.setCategory(nodePost.getCategory());
        existingPost.setTags(nodePost.getTags());
        existingPost.setPostTime(new Date());
        existingPost.setStatus(0); // 重置为待审核状态
        
        try {
            // 查询原有图片记录
            LambdaQueryWrapper<NodeImage> oldImagesWrapper = new LambdaQueryWrapper<>();
            oldImagesWrapper.eq(NodeImage::getId, nodePost.getId());
            List<NodeImage> oldImages = nodeImageMapper.selectList(oldImagesWrapper);
            
            // 收集要删除的图片ID
            List<Integer> keepImageIds = imageList.stream()
                    .filter(img -> img.getId() != null)
                    .map(NodeImage::getId)
                    .collect(Collectors.toList());
            
            // 删除不再使用的图片
            for (NodeImage oldImage : oldImages) {
                if (!keepImageIds.contains(oldImage.getId())) {
                    // 从磁盘删除图片文件
                    String filePath = notePath + oldImage.getImagePath();
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    
                    // 从数据库删除记录
                    nodeImageMapper.deleteById(oldImage.getId());
                    
                    // 如果删除的是封面图片，需要更新封面
                    if (oldImage.getImagePath().equals(existingPost.getCoverImage())) {
                        existingPost.setCoverImage(null);
                    }
                }
            }
            
            // 创建基于日期的文件夹路径，用于保存新图片
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateFolder = sdf.format(new Date());
            String datePath = notePath + dateFolder + "/";
            
            // 确保目录存在
            File dateDir = new File(datePath);
            if (!dateDir.exists()) {
                dateDir.mkdirs();
            }
            
            // 处理新图片文件
            int newFileIndex = 0;
            boolean needsCover = existingPost.getCoverImage() == null;
            
            // 处理imageList中的图片
            for (NodeImage imageItem : imageList) {
                if (imageItem.getId() == null) {
                    // 这是新图片，需要保存文件
                    if (newFileIndex < newImageFiles.length) {
                        MultipartFile imageFile = newImageFiles[newFileIndex++];
                        
                        if (imageFile != null && !imageFile.isEmpty()) {
                            // 生成唯一文件名
                            String originalFilename = imageFile.getOriginalFilename();
                            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                            String fileName = UUID.randomUUID().toString() + suffix;
                            String filePath = datePath + fileName;
                            
                            // 保存文件
                            File file = new File(filePath);
                            imageFile.transferTo(file);
                            String imageUrl = dateFolder + "/" + fileName;
                            
                            // 如果需要设置新封面
                            if (needsCover) {
                                existingPost.setCoverImage(imageUrl);
                                needsCover = false;
                            }
                            
                            // 添加到图片记录
                            imageItem.setNodeId(nodePost.getId());
                            imageItem.setImagePath(imageUrl);
                            nodeImageMapper.insert(imageItem);
                        }
                    }
                }
            }
            
            // 更新帖子信息
            nodePostMapper.updateById(existingPost);
            
            // 发送审核消息
            Message message = new Message();
            message.setTime(new Date());
            message.setContent("用户：" + nodePost.getUserId() + "更新了笔记，请审核");
            message.setUser(nodePost.getUserId());
            message.setToUser("admin");
            message.setType("SYSTEM-ADMIN");
            messageMapper.insert(message);
            redisComponent.setMessage(message);
            
            // 返回更新后的帖子信息
            NotePostDto notePostDto = CopyTools.copy(existingPost, NotePostDto.class);
            
            // 查询更新后的图片列表
            LambdaQueryWrapper<NodeImage> updatedImagesWrapper = new LambdaQueryWrapper<>();
            updatedImagesWrapper.eq(NodeImage::getId, nodePost.getId());
            List<NodeImage> updatedImages = nodeImageMapper.selectList(updatedImagesWrapper);
            notePostDto.setImages(updatedImages);
            
            return notePostDto;
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("更新笔记时发生错误", e);
        }
    }
    
    @Override
    @Transactional
    public boolean deletePostNode(String postId, String userId) {
        // 先查询确认帖子存在并属于当前用户
        NodePost post = nodePostMapper.selectById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
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
            
            // 4. 删除帖子
            nodePostMapper.deleteById(postId);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除笔记时发生错误", e);
        }
    }
    
    @Override
    public NotePostDto getPostNode(String postId) {
        // 查询帖子信息
        NodePost post = nodePostMapper.selectById(postId);
        if (post == null) {
            return null;
        }
        
        // 复制到DTO
        NotePostDto notePostDto = CopyTools.copy(post, NotePostDto.class);
        
        // 查询相关图片
        LambdaQueryWrapper<NodeImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NodeImage::getId, postId);
        List<NodeImage> images = nodeImageMapper.selectList(wrapper);
        
        // 处理图片URL，添加前缀
        for (NodeImage image : images) {
            // 将相对路径转换为完整URL
            String fullImageUrl = hostUrl + "/image/note/" + image.getImagePath();
            image.setImagePath(fullImageUrl);
        }
        
        // 设置完整的NodeImage对象列表
        notePostDto.setImages(images);
        
        // 处理封面图片URL
        if (notePostDto.getCoverImage() != null && !notePostDto.getCoverImage().isEmpty()) {
            notePostDto.setCoverImage(hostUrl + "/image/note/" + notePostDto.getCoverImage());
        }
        
        return notePostDto;
    }
    
    @Override
    public Page<NotePostDto> getPostNodeList(Integer page, Integer size, Integer categoryId, String keyword) {
        // 创建分页对象
        Page<NodePost> pageParam = new Page<>(page, size);
        
        // 创建查询条件
        LambdaQueryWrapper<NodePost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NodePost::getStatus, 1); // 只查询已审核通过的
        
        // 添加分类条件
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(NodePost::getCategory, categoryId);
        }
        
        // 添加关键词搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(NodePost::getTitle, keyword)
                    .or()
                    .like(NodePost::getContent, keyword)
                    .or()
                    .like(NodePost::getTags, keyword));
        }
        
        // 按发布时间倒序排序
        wrapper.orderByDesc(NodePost::getPostTime);
        
        // 执行分页查询
        Page<NodePost> result = nodePostMapper.selectPage(pageParam, wrapper);
        
        // 转换为DTO对象
        Page<NotePostDto> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<NotePostDto> records = result.getRecords().stream()
                .map(post -> CopyTools.copy(post, NotePostDto.class))
                .collect(Collectors.toList());
        dtoPage.setRecords(records);
        
        return dtoPage;
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
                .map(post -> CopyTools.copy(post, NotePostDto.class))
                .collect(Collectors.toList());
        dtoPage.setRecords(records);
        
        return dtoPage;
    }
}
