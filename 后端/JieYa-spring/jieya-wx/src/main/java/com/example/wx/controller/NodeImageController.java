package com.example.wx.controller;

import com.example.common.common.Result;
import com.example.common.constants.Constants;
import com.example.common.pojo.NodeImage;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.JwtUtil;
import com.example.common.utils.StringTools;
import com.example.wx.service.NodeImageService;
import com.example.wx.service.NodePostService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@RestController
@RequestMapping("/node-image")
public class NodeImageController {

    @Autowired
    private NodeImageService nodeImageService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${upload.tempPath}")
    private String tempPath;

    @Autowired
    private RedisComponent redisComponent;
    
    /**
     * 上传额外的笔记图片
     */
    @RequestMapping("/upload")
    public Result upload(@NotNull MultipartFile file,
                         @NotNull Integer sort,
                         String noteId) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            // 确保临时目录存在
            File tempDir = new File(tempPath);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            // 保存到临时目录
            String tempFileName = StringTools.getRandomBumber(10) + "_" + file.getOriginalFilename();
            File tempFile = new File(tempPath, tempFileName);
            file.transferTo(tempFile);
            
            NodeImage nodeImage = new NodeImage();
            if(noteId==null){
                noteId = StringTools.getRandomBumber(Constants.LENGTH_10);
            }
            nodeImage.setNodeId(noteId);
            nodeImage.setSort(sort);
            nodeImage.setImagePath(tempFile.getAbsolutePath());
            redisComponent.setTempNoteImage(nodeImage);
            
            return Result.success(nodeImage);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}

