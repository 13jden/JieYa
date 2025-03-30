package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.admin.service.VenueImageService;
import com.example.common.common.Result;
import com.example.common.pojo.VenueImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@RestController
@RequestMapping("/venue-image")
public class VenueImageController {

    @Value("${upload.venueFilePath}")
    private String uploadPath;
    
    @Value("${host.url}")
    private String hostUrl;

    @Autowired
    private VenueImageService venueImageService;
    /**
     * 获取场馆的非封面图片
     */
    @GetMapping("/other-images/{venueId}")
    public Result getOtherImages(@PathVariable Integer venueId) {
        // 查询除封面外的其他图片
        QueryWrapper<VenueImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("venue_id", venueId)
                    .ne("sort_order", 1)
                    .orderByAsc("sort_order");
        
        List<VenueImage> images = venueImageService.list(queryWrapper);
        return Result.success(images);
    }

}

