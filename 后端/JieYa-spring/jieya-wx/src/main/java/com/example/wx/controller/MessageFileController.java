package com.example.wx.controller;

import com.example.common.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * 消息文件控制器
 * </p>
 */
@RestController
@RequestMapping("/message/file")
public class MessageFileController {

    @Value("${upload.messageFilePath}")
    private String messageFilePath;

    /**
     * 上传消息文件，按日期分类存储
     * @param file 消息文件
     * @return 上传结果，包含文件路径
     */
    @PostMapping("/upload")
    public Result uploadMessageFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        try {
            // 按当前日期创建子目录
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String today = dateFormat.format(new Date());
            
            // 构建日期子目录
            File dateDir = new File(messageFilePath, today);
            if (!dateDir.exists()) {
                if (!dateDir.mkdirs()) {
                    return Result.error("创建日期目录失败");
                }
            }
            
            // 生成文件名：时间戳_原文件名
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File destFile = new File(dateDir, fileName);
            
            // 保存文件
            file.transferTo(destFile);
            
            // 返回相对路径，可以通过API访问
            String filePath = "message/" + today + "/" + fileName;
            return Result.success(filePath);
            
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除消息文件
     * @param filePath 文件路径，格式为：message/20250401/1234567890_filename.jpg
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result deleteMessageFile(@RequestParam("filePath") String filePath) {
        // 从路径中提取日期和文件名
        if (filePath == null || !filePath.startsWith("message/")) {
            return Result.error("无效的文件路径");
        }
        
        String[] parts = filePath.split("/");
        if (parts.length != 3) {
            return Result.error("无效的文件路径格式");
        }
        
        String date = parts[1];
        String fileName = parts[2];
        
        File file = new File(messageFilePath + "/" + date, fileName);
        if (file.exists() && file.delete()) {
            return Result.success("文件删除成功");
        }
        return Result.error("文件删除失败或文件不存在");
    }
} 