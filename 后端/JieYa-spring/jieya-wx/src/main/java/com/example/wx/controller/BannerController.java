package com.example.wx.controller;

import com.example.common.common.Result;
import com.example.common.pojo.Banner;
import com.example.wx.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  Banner前端控制器
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
    @GetMapping("/list")
    public Result<List<Banner>> getList() {
        List<Banner> banners = bannerService.getList();
        return Result.success(banners);
    }
}

