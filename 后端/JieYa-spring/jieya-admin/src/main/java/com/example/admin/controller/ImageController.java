package com.example.admin.controller;

import com.example.common.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * 图片控制器
 * </p>
 */
@RestController
@RequestMapping("/image")
public class ImageController {

    @Value("${upload.tempPath}")
    private String tempPath;

    @Value("${upload.bannerFilePath}")
    private String bannerFilePath;

    /**
     * 上传图片到临时目录
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            // 保存到临时目录
            String tempFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File tempFile = new File(tempPath, tempFileName);
            file.transferTo(tempFile);
            return Result.success(tempPath + "/" + tempFileName);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除临时文件
     * @param fileName 文件名
     * @return 删除结果
     */
    @DeleteMapping("/deleteTemp")
    public Result deleteTemp(@RequestParam("fileName") String fileName) {
        File tempFile = new File(tempPath, fileName);
        if (tempFile.exists() && tempFile.delete()) {
            return Result.success("临时文件删除成功");
        }
        return Result.error("临时文件删除失败");
    }

    @DeleteMapping("/deleteBanner")
    public Result deleteBanner(@RequestParam("fileName") String fileName) {
        File FilePath = new File(bannerFilePath, fileName);
        if (FilePath.exists() && FilePath.delete()) {
            return Result.success("文件删除成功");
        }
        return Result.error("文件删除失败");
    }


} 