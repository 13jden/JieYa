package com.example.wx.controller;


import com.example.common.common.Result;
import com.example.common.utils.JwtUtil;
import com.example.wx.service.NodeService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/note")
public class NodeController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private JwtUtil jwtUtil;

    @RequestMapping("/search")
    public Result searchNotes(@RequestParam String keyword, @RequestParam int pageNum, @RequestParam int pageSize, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        String userId = jwtUtil.getUserId(token);
        return Result.success(nodeService.searchByKeyword(userId, keyword, pageNum, pageSize));
    }

    @RequestMapping("/list")
    public Result getList(@RequestParam int pageNum, @RequestParam int pageSize, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        String userId = jwtUtil.getUserId(token);
        return Result.success(nodeService.getList(userId,pageNum,pageSize));
    }

    @RequestMapping("/detail")
    public Result getDetail(@RequestParam String id,HttpServletRequest request){
        String token = request.getHeader("Authorization");
        String userId = jwtUtil.getUserId(token);
        return Result.success(nodeService.getDetail(userId,id));
    }


}

