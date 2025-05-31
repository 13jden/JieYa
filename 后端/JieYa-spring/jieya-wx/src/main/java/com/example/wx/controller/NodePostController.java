package com.example.wx.controller;

import com.alibaba.fastjson.JSON;
import com.example.common.adminDto.NotePostDto;
import com.example.common.common.Result;
import com.example.common.pojo.NodeImage;
import com.example.common.pojo.NodePost;
import com.example.common.utils.JwtUtil;
import com.example.wx.service.NodePostService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@RestController
@RequestMapping("/node-post")
public class NodePostController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NodePostService nodePostService;

    @PostMapping("/postNode")
    public Result add(@NotEmpty String title,
                      @NotEmpty String noteId,
                      @NotNull Integer imageCount,
                      String content,
                      HttpServletRequest request,
                      Integer categoryId,
                      String[] tags) {
        
        // 添加详细日志输出
        System.out.println("收到笔记提交请求:");
        System.out.println("标题: " + title);
        System.out.println("内容: " + content);
        System.out.println("分类ID: " + (categoryId != null ? categoryId : "null"));
        System.out.println("标签数: " + (tags != null ? tags.length : "null"));
        
        String userId = jwtUtil.getUserId(request.getHeader("Authorization"));
        NodePost nodePost = new NodePost();
        nodePost.setUserId(userId);
        nodePost.setPostTime(new Date());
        nodePost.setContent(content);
        nodePost.setId(noteId);
        // 处理tags可能为null的情况
        if (tags == null) {
            tags = new String[0];
        }
        nodePost.setTags(JSON.toJSONString(tags));
        nodePost.setCategory(categoryId != null ? categoryId : 1);
        nodePost.setTitle(title);
        
        NotePostDto nodePost1 = nodePostService.addPostNode(nodePost,imageCount);

        if(nodePost1 != null)
            return Result.success(nodePost1);

        return Result.error("添加错误");
    }
    
    @PutMapping("/updateNode")
    public Result update(@NotEmpty String postId,
                        @NotEmpty String title,
                        String content,
                        HttpServletRequest request,
                        Integer categoryId,
                        String[] tags,
                        @RequestBody List<NodeImage> imageList,
                        MultipartFile[] newImageFiles) {
        String userId = jwtUtil.getUserId(request.getHeader("Authorization"));
        
        // 先查询确认帖子存在并属于当前用户
        NotePostDto existingPost = nodePostService.getPostNode(postId);
        if (existingPost == null) {
            return Result.error("笔记不存在");
        }
        
        if (!existingPost.getUserId().equals(userId)) {
            return Result.error("无权修改此笔记");
        }
        
        NodePost nodePost = new NodePost();
        nodePost.setId(postId);
        nodePost.setUserId(userId);
        nodePost.setContent(content);
        nodePost.setTags(JSON.toJSONString(tags));
        nodePost.setCategory(categoryId != null ? categoryId : 1);
        nodePost.setTitle(title);
        
        NotePostDto updatedPost = nodePostService.updatePostNode(nodePost, newImageFiles, imageList);
        if (updatedPost != null) {
            return Result.success(updatedPost);
        }
        
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/deleteNode")
    public Result delete(@NotEmpty String postId, HttpServletRequest request) {
        String userId = jwtUtil.getUserId(request.getHeader("Authorization"));
        
        boolean success = nodePostService.deletePostNode(postId, userId);
        if (success) {
            return Result.success("删除成功");
        }
        
        return Result.error("删除失败或无权限");
    }
    
    @GetMapping("/detail")
    public Result detail(@NotEmpty String postId) {
        NotePostDto post = nodePostService.getPostNode(postId);
        if (post != null) {
            return Result.success(post);
        }
        
        return Result.error("笔记不存在");
    }
    
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) Integer categoryId,
                      @RequestParam(required = false) String keyword) {
        Page<NotePostDto> posts = nodePostService.getPostNodeList(page, size, categoryId, keyword);
        return Result.success(posts);
    }
    
    @GetMapping("/myList")
    public Result myList(@RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer size,
                        HttpServletRequest request) {
        String userId = jwtUtil.getUserId(request.getHeader("Authorization"));
        Page<NotePostDto> posts = nodePostService.getUserPostNodeList(userId, page, size);
        return Result.success(posts);
    }
}

