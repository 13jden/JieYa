package com.example.wx.controller;

import com.example.common.adminDto.VenueDto;

import com.example.common.common.Result;
import com.example.common.redis.RedisComponent;
import com.example.wx.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private RedisComponent redisComponent;

    /**
     * 获取所有场地列表（仅包含基本信息和封面图）
     */
    @GetMapping("/list")
    public Result listVenues() {
        List<VenueDto> venues = redisComponent.getVenueList();
        if(venues==null){
            try {
             venues = venueService.listVenuesWithCoverImages();
             redisComponent.setVenueList(venues);
                return Result.success(venues);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("获取场地列表失败");
            }
        }
        return Result.success(venues);
    }

    /**
     * 获取场地详情
     */
    @GetMapping("/detail/{id}")
    public Result getVenueDetail(@PathVariable Integer id) {
        VenueDto venue = redisComponent.getVenue(id);
        if(venue==null){
            try {
             venue= venueService.getVenueWithImagesById(id);
                if (venue == null) {
                    return Result.error("场地不存在");
                }
                return Result.success(venue);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("获取场地详情失败");
            }
        }
        return Result.success(venue);
    }
    
    /**
     * 按标签筛选场地
     */
    @GetMapping("/tag/{tagId}")
    public Result getVenuesByTag(@PathVariable Integer tagId) {
        try {
            List<VenueDto> venues = venueService.listVenuesByTagId(tagId);
            return Result.success(venues);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("按标签获取场地失败");
        }
    }
    
    /**
     * 搜索场地
     */
    @GetMapping("/search")
    public Result searchVenues(@RequestParam String keyword) {
        try {
            List<VenueDto> venues = venueService.searchVenues(keyword);
            return Result.success(venues);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索场地失败");
        }
    }
}

