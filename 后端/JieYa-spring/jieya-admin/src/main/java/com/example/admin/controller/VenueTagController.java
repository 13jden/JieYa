package com.example.admin.controller;

import com.example.admin.service.VenueTagService;
import com.example.common.common.Result;
import com.example.common.pojo.VenueTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@RestController
@RequestMapping("/venue-tag")
public class VenueTagController {

    @Autowired
    private VenueTagService venueTagService;

    /**
     * 添加场馆标签
     */
    @PostMapping("/add")
    public Result addTag(@RequestParam String tagName) {
        // 检查标签名是否已存在
        if (venueTagService.existsByName(tagName)) {
            return Result.error("标签名已存在");
        }
        VenueTag tag = new VenueTag();
        tag.setTagName(tagName);
        if (venueTagService.save(tag)) {
            return Result.success(tag);
        } else {
            return Result.error("标签添加失败");
        }
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/list")
    public Result listTags() {
        List<VenueTag> tags = venueTagService.list();
        return Result.success(tags);
    }

    /**
     * 删除标签
     */
    @PostMapping("/delete")
    public Result deleteTag(@RequestParam Integer tagId) {
        // 可以添加检查，确保标签没有被场馆使用
        if (venueTagService.isTagInUse(tagId)) {
            return Result.error("该标签正在使用中，无法删除");
        }
        if (venueTagService.removeById(tagId)) {
            return Result.success("标签删除成功");
        } else {
            return Result.error("标签删除失败");
        }
    }
}