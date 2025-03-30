package com.example.wx.controller;

import com.example.common.adminDto.PropDto;
import com.example.common.common.Result;
import com.example.common.redis.RedisComponent;
import com.example.wx.service.PropService;
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
@RequestMapping("/prop")
public class PropController {

    @Autowired
    private PropService propService;

    @Autowired
    private RedisComponent redisComponent;
    /**
     * 获取所有道具列表（仅包含基本信息和封面图）
     */
    @GetMapping("/list")
    public Result listProps() {
        List<PropDto> props = redisComponent.getPropList();
        if(props==null){
            try {
             props = propService.listPropsWithCoverImages();
             redisComponent.setPropList(props);
                return Result.success(props);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("获取道具列表失败");
            }
        }
        return Result.success(props);
    }

    /**
     * 获取道具详情
     */
    @GetMapping("/detail/{id}")
    public Result getPropDetail(@PathVariable Integer id) {
        PropDto prop = redisComponent.getProp(id);
        if(prop==null){
            try {
                prop = propService.getPropWithImagesById(id);
                redisComponent.setProp(prop);
                if (prop == null) {
                    return Result.error("道具不存在");
                }
                return Result.success(prop);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("获取道具详情失败");
            }
        }
        return Result.success(prop);
    }
    
    /**
     * 搜索道具
     */
    @GetMapping("/search")
    public Result searchProps(@RequestParam String keyword) {
        try {
            List<PropDto> props = propService.searchProps(keyword);
            return Result.success(props);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索道具失败");
        }
    }
    
    /**
     * 获取可用道具列表
     */
    @GetMapping("/available")
    public Result listAvailableProps() {
        try {
            List<PropDto> props = propService.listAvailableProps();
            return Result.success(props);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取可用道具列表失败");
        }
    }
}

