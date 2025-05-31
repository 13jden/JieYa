package com.example.wx.controller;


import com.example.common.WxDto.ActiveMessageDto;
import com.example.common.common.Result;
import com.example.common.constants.Constants;
import com.example.common.pojo.Comment;
import com.example.common.pojo.Message;
import com.example.common.pojo.Node;
import com.example.common.pojo.User;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.common.utils.JwtUtil;
import com.example.common.utils.StringTools;
import com.example.wx.mapper.MessageMapper;
import com.example.wx.mapper.UserMapper;
import com.example.wx.service.CommentService;
import com.example.wx.service.MessageService;
import com.example.wx.service.NodeService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.ArrayList;

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
    private NodeService nodeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("upload.commentPath")
    private String commentPath;

    @Value("host.url")
    private String hostUrl;


//    @RequestMapping("/getList")
//    public Result getList(@RequestParam int pageNum , @RequestParam int pageSize){
//        return Result.success(commentService.getList(pageNum,pageSize));
//    }

    @RequestMapping("/getChildren")
    public Result getCommentChildren(@RequestParam String commentParentId,
                                    @RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(commentService.getChildren(commentParentId, pageNum, pageSize));
    }

    @RequestMapping("/add")
    @Transactional
    public Result addComment(String content,
                             @NotEmpty String noteId,
                             Integer parentId,
                             String toUser,//如果是回复父评论，则不传只传父id(从父id去找user),回复子评论才传
                             MultipartFile imageFile,
                             @RequestHeader("Authorization") String token){
        if(imageFile==null&&content==null){
            throw new IllegalArgumentException("评论不能为空");
        }

        String userId = jwtUtil.getUserId(token);
        System.out.println(userId);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setIsTop(0);
        comment.setLikeCount(0); // 初始点赞数为0
        comment.setNoteId(noteId); // 设置所属笔记
        comment.setTime(new Date()); // 设置创建时间

        if (parentId!=null && toUser != null) {//说明是回复子评论
            comment.setToUserId(toUser);
            comment.setParentId(parentId);
        }
        if(parentId!=null&&toUser==null){//说明是回复父评论的，而不是在子评论里回复,在保存以后再赋值
            comment.setParentId(parentId);
        }else{//回复视频
            Node node = nodeService.getById(noteId);
            toUser = node.getUserId();
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 创建日期格式文件夹
                String dateFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String targetPath = commentPath + "/" + dateFolder;
                File folder = new File(targetPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                // 生成唯一文件名
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = UUID.randomUUID() + fileExtension;
                String relativePath = dateFolder + "/" + newFilename;
                // 保存文件
                File targetFile = new File(targetPath + "/" + newFilename);
                imageFile.transferTo(targetFile);
                // 设置评论图片URL
                comment.setFileUrl(relativePath);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("图片上传失败");
            }
        }
        
        // 保存评论
        commentService.save(comment);
        if(parentId!=null&&toUser==null){//说明是回复父评论的，而不是在子评论里回复,在保存以后再赋值
            Comment comment1 = commentService.getById(parentId);
            toUser = comment1.getUserId();
        }

        // 处理返回的图片URL，添加前缀
        if (comment.getFileUrl() != null) {
            comment.setFileUrl(hostUrl + "/images/comment/" + comment.getFileUrl());
        }
        // 发送消息通知
        if (!toUser.equals(userId)) {//评论自己不发送消息
            System.out.println("id不一样"+userId+"   "+toUser);
            User targetUser = userMapper.selectById(toUser);
            ActiveMessageDto activeMessageDto = redisComponent.getUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE +"comment:"+ noteId + parentId);

            if (targetUser != null && activeMessageDto == null) {
                Message message = new Message();
                message.setUser("system");
                message.setToUser(toUser);
                message.setType("SYSTEM-USER");
                message.setNoteId(noteId);
                String messageContent = "用户：" + targetUser.getNickName() + "评论了你";
                message.setContent(messageContent);
                message.setTime(new Date());
                message.setStatus(0);
                messageService.save(message);
                
                activeMessageDto = CopyTools.copy(message, ActiveMessageDto.class);
                // 初始化 userName 列表
                activeMessageDto.setUserName(new ArrayList<>());
                activeMessageDto.setUserCount(1);
                activeMessageDto.getUserName().add(targetUser.getNickName());
                redisComponent.setMessage(message);
                redisComponent.setUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE + "comment:" + noteId + parentId, activeMessageDto);
            } else if (targetUser != null) { // 十分钟内有消息
                String messageContent = targetUser.getNickName() + "," + 
                        activeMessageDto.getUserName().get(activeMessageDto.getUserName().size()-1) + 
                        "等" + (activeMessageDto.getUserCount()+1) + "位用户评论了你";
                activeMessageDto.setContent(messageContent);
                activeMessageDto.getUserName().add(targetUser.getNickName());
                activeMessageDto.setUserCount(activeMessageDto.getUserCount()+1);
                Message message = CopyTools.copy(activeMessageDto, Message.class);
                message.setContent(messageContent);
                redisComponent.setMessage(message);
                redisComponent.setUserActiveMessage(Constants.REDIS_KEY_ACTIVE_MESSAGE + "comment:" + noteId + parentId, activeMessageDto);
            }
        }
        
        return Result.success(comment);
    }

    @RequestMapping("/delete")
    public Result deleteComment(@RequestParam String commentId, 
                                @RequestHeader("Authorization") String token) {
        String userId = jwtUtil.getUserId(token);
        
        // 查询评论
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 验证权限：评论作者或笔记作者才能删除
        if (!comment.getUserId().equals(userId)) {
            // 如果不是评论作者，检查是否是笔记作者
            Node note = nodeService.getById(comment.getNoteId());
            if (note == null || !note.getUserId().equals(userId)) {
                return Result.error("没有权限删除此评论");
            }
        }
        
        // 删除评论及其子评论
        boolean success = commentService.removeCommentWithChildren(commentId);
        
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @RequestMapping("/getListByNoteId")
    public Result getListByNoteId(@RequestParam String noteId,
                                 @RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(commentService.getListByNoteId(noteId, pageNum, pageSize));
    }

}

