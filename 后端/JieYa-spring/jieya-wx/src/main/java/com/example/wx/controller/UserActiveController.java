package com.example.wx.controller;


import com.example.common.common.Result;
import com.example.common.utils.JwtUtil;
import com.example.wx.service.UserActiveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
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
@RequestMapping("/user-active")
public class UserActiveController {

    @Autowired
    private UserActiveService userActiveService;

    @Autowired
    private JwtUtil jwtUtil;

    @RequestMapping("checkActive")
    public Result checkUserActive(@NotEmpty String noteId,
                                  HttpServletRequest request){
        String token = request.getHeader("Authorization");
        String userId = jwtUtil.getUserId(token);
        return Result.success(userActiveService.checkUserActive(userId,noteId));
    }

//    @RequestMapping("/getLikeList")
//    public Result getLikeList(@RequestParam String userId) {
//        return Result.success(userActiveService.getLikeList(userId));
//    }

    @RequestMapping("/getCollectList")
    public Result getCollectList(@RequestParam String userId) {
        return Result.success(userActiveService.getCollectList(userId));
    }

    @RequestMapping("/addLike")
    public Result addLike(@RequestParam String noteId, @RequestParam String toUserId,HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = jwtUtil.getUserId(token);
        return Result.success(userActiveService.addLike(userId, noteId, toUserId));
    }

    @RequestMapping("/deleteLike")
    public Result deleteLike(@RequestParam String noteId,@RequestParam String toUserId,HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String userId = jwtUtil.getUserId(token);
        return Result.success(userActiveService.deleteLike(userId, noteId,toUserId));
    }

    @RequestMapping("/addCollect")
    public Result addCollect(@RequestParam String userId, @RequestParam String noteId) {
        return Result.success(userActiveService.addCollect(userId, noteId));
    }

    @RequestMapping("/deleteCollect")
    public Result deleteCollect(@RequestParam String userId, @RequestParam String noteId) {
        return Result.success(userActiveService.deleteCollect(userId, noteId));
    }
}
