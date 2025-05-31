package com.example.wx.controller;


import com.example.common.common.Result;
import com.example.wx.service.FocusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@RestController
@RequestMapping("/focus")
public class FocusController {
    @Autowired
    private FocusService focusService;

    @RequestMapping("/add")
    public Result addFocus(@RequestParam String userId, @RequestParam String focusUserId) {
        return Result.success(focusService.addFocus(userId, focusUserId));
    }

    @RequestMapping("/delete")
    public Result deleteFocus(@RequestParam String userId, @RequestParam String focusUserId) {
        return Result.success(focusService.deleteFocus(userId, focusUserId));
    }

    @RequestMapping("/getList")
    public Result getFocusList(@RequestParam String userId) {
        return Result.success(focusService.getFocusList(userId));
    }

    @RequestMapping("/getFansList")
    public Result getFansList(@RequestParam String userId) {
        return Result.success(focusService.getFansList(userId));
    }


}

