package com.example.admin.controller;


import com.example.admin.mapper.UserMapper;
import com.example.admin.service.CommentService;
import com.example.common.common.Result;
import com.example.common.pojo.Comment;
import com.example.common.pojo.Message;
import com.example.common.pojo.User;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-04-03
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("upload.commentPath")
    private String commentPath;


    @RequestMapping("/getList")
    public Result getList(@RequestParam int pageNum , @RequestParam int pageSize){
        return Result.success(commentService.getList(pageNum,pageSize));
    }


}

