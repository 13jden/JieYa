package com.example.wx.controller;


import com.example.common.common.Result;
import com.example.common.pojo.VenueImage;
import com.example.common.pojo.VenueTag;
import com.example.wx.service.VenueTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/list")
    public Result listTags() {
        List<VenueTag> tags = venueTagService.list();
        return Result.success(tags);
    }
}

