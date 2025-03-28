package com.example.admin.controller;

import com.example.admin.service.BannerService;
import com.example.common.common.Result;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Banner前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@RestController
@RequestMapping("/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取Banner列表
     * @return Banner列表
     */
    @RequestMapping("/getList")
    public Result getList() {
        return Result.success(bannerService.getList());
    }

    /**
     * 添加Banner
     * @param image 图片
     * @param type 类型：1为场地，2为商品，3为笔记
     * @param on 是否启用：1是，0否
     * @param text 标题（可选）
     * @param contentId 跳转ID（可选）
     * @return 添加结果
     */
    @RequestMapping("/add")
    public Result add(
            @NotEmpty String image,
            @NotNull Integer type,
            Integer on,
            String text,
            Integer contentId) {
        System.out.println("添加");
        return Result.success(bannerService.add(image, type, on, text, contentId));
    }

    /**
     * 更新Banner
     * @param bannerId Banner ID
     * @param image 图片
     * @param type 类型：1为场地，2为商品，3为笔记
     * @param on 是否启用：1是，0否
     * @param text 标题（可选）
     * @param contentId 跳转ID（可选）
     * @return 更新结果
     */
    @RequestMapping("/update")
    public Result update(
            @NotNull Integer bannerId,
            String image,
            Integer type,
            Integer on,
            String text,
            Integer contentId) {
        return Result.success(bannerService.update(bannerId, image, type, on, text, contentId));
    }

    /**
     * 删除Banner
     * @param bannerId Banner ID
     * @return 删除结果
     */
    @RequestMapping("/delete")
    public Result delete(@NotNull Integer bannerId) {
        return Result.success(bannerService.removeById(bannerId));
    }

    /**
     * 更新Banner排序
     * @param sortList 排序列表，包含bannerId和sort
     * @return 更新结果
     */
    @RequestMapping("/sort")
    public Result sort(@RequestBody @NotEmpty List<Map<String, Integer>> sortList) {
        return Result.success(bannerService.updateSort(sortList));
    }
}

