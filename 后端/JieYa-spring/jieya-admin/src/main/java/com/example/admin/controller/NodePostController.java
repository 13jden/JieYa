package com.example.admin.controller;

import com.example.admin.service.NodePostService;
import com.example.common.adminDto.NotePostDto;
import com.example.common.common.Result;
import com.example.common.pojo.NodePost;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private NodePostService nodePostService;

    /**
     * 获取待审核的笔记列表
     */
    @GetMapping("/pendingList")
    public Result getPendingList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        Page<NotePostDto> posts = nodePostService.getPendingPostNodeList(page, size);
        return Result.success(posts);
    }

    /**
     * 获取所有笔记列表（分页）
     */
    @GetMapping("/list")
    public Result getList(@RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer size,
                        @RequestParam(required = false) Integer categoryId,
                        @RequestParam(required = false) String keyword) {
        Page<NotePostDto> posts = nodePostService.getPostNodeList(page, size, categoryId, keyword);
        return Result.success(posts);
    }

    /**
     * 按标题搜索笔记
     */
    @GetMapping("/searchByTitle")
    public Result searchByTitle(@RequestParam String title,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size) {
        Page<NotePostDto> posts = nodePostService.searchPostNodeByTitle(title, page, size);
        return Result.success(posts);
    }

    /**
     * 获取笔记详情
     */
    @GetMapping("/detail")
    public Result getDetail(@NotEmpty @RequestParam String postId) {
        NotePostDto post = nodePostService.getPostNodeDetail(postId);
        if (post != null) {
            return Result.success(post);
        }
        return Result.error("笔记不存在");
    }

    /**
     * 审核通过笔记
     */
    @PostMapping("/approve")
    public Result approvePost(@NotEmpty @RequestParam String postId) {
        boolean success = nodePostService.approvePostNode(postId);
        if (success) {
            return Result.success("审核通过成功");
        }
        return Result.error("审核通过失败");
    }

    /**
     * 驳回笔记
     */
    @PostMapping("/reject")
    public Result rejectPost(@NotEmpty @RequestParam String postId, 
                           @RequestParam String reason) {
        boolean success = nodePostService.rejectPostNode(postId, reason);
        if (success) {
            return Result.success("驳回成功");
        }
        return Result.error("驳回失败");
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/delete")
    public Result deletePost(@NotEmpty @RequestParam String postId) {
        boolean success = nodePostService.deletePostNode(postId);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 按用户ID查询笔记列表
     */
    @GetMapping("/listByUser")
    public Result getPostsByUser(@NotEmpty @RequestParam String userId,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        Page<NotePostDto> posts = nodePostService.getUserPostNodeList(userId, page, size);
        return Result.success(posts);
    }
    
    /**
     * 修改笔记分类
     */
    @PutMapping("/updateCategory")
    public Result updateCategory(@NotEmpty @RequestParam String postId,
                               @RequestParam Integer categoryId) {
        boolean success = nodePostService.updatePostNodeCategory(postId, categoryId);
        if (success) {
            return Result.success("更新分类成功");
        }
        return Result.error("更新分类失败");
    }
}

