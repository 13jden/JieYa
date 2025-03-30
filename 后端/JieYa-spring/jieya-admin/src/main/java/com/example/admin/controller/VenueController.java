package com.example.admin.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.admin.service.VenueImageService;
import com.example.admin.service.VenueService;
import com.example.admin.service.VenueTagService;
import com.example.common.adminDto.VenueDto;
import com.example.common.common.Result;
import com.example.common.pojo.Venue;
import com.example.common.pojo.VenueTag;
import com.example.common.pojo.VenueImage;
import com.example.common.redis.RedisComponent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.common.adminDto.ImageDto;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@RestController
@RequestMapping("/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueTagService venueTagService;

    @Autowired
    private RedisComponent redisComponent;

    @Autowired
    private VenueImageService venueImageService;

    @Value("${upload.venueFilePath}")
    private String uploadPath;

    @Value("${host.url}")
    private String hostUrl;

    @PostMapping("/add")
    public Result addVenue(@NotEmpty String venueName,
                           String location,
                           @NotNull BigDecimal price,
                           Integer capacity,
                           String introduction,
                           Integer[] tagIds,
                           String imageDtosJson,
                           MultipartFile[] files) {
        
        try {
            // 创建Venue对象
            Venue venue = new Venue();
            venue.setName(venueName);
            if(location != null) {
                venue.setLocation(location);
            }
            venue.setPrice(price);
            venue.setCapacity(capacity);
            venue.setContent(introduction);
            
            // 处理标签
            if(tagIds != null && tagIds.length > 0) {
                venue.setTags(JSON.toJSONString(tagIds));
            } else {
                venue.setTags("[]");
            }
            
            venue.setCreatedAt(new Date());
            venue.setUpdatedAt(new Date());
            
            // 保存场地基本信息
            boolean success = venueService.save(venue);
            if (!success) {
                return Result.error("场地添加失败");
            }
            
            // 处理图片文件并保存到VenueImage表
            List<VenueImage> venueImages = new ArrayList<>();
            List<String> savedImageNames = new ArrayList<>();
            
            if (imageDtosJson != null && files != null && files.length > 0) {
                // 解析图片信息JSON
                ObjectMapper mapper = new ObjectMapper();
                List<ImageDto> imageDtos = mapper.readValue(imageDtosJson, 
                        mapper.getTypeFactory().constructCollectionType(List.class, ImageDto.class));
                
                // 确保目录存在
                File dir = new File(uploadPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                // 处理每张图片，根据DTO中的信息设置属性
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    ImageDto imageDto = (i < imageDtos.size()) ? imageDtos.get(i) : null;
                    
                    if (!file.isEmpty()) {
                        // 生成唯一的文件名
                        String originalFilename = file.getOriginalFilename();
                        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String fileName = System.currentTimeMillis() + suffix;
                        
                        // 保存文件
                        File dest = new File(uploadPath + "/" + fileName);
                        file.transferTo(dest);
                        
                        // 创建VenueImage对象
                        VenueImage venueImage = new VenueImage();
                        venueImage.setVenueId(venue.getId());
                        venueImage.setImageUrl(fileName);
                        
                        // 根据DTO设置排序
                        if (imageDto != null) {
                            venueImage.setSortOrder(imageDto.getSortOrder());
                        } else {
                            // 默认设置
                            venueImage.setSortOrder(i + 1);
                        }
                        
                        venueImage.setCreatedAt(new Date());
                        
                        venueImages.add(venueImage);
                        savedImageNames.add(fileName);
                        
                        // 防止文件名重复
                        Thread.sleep(1);
                    }
                }
                
                // 批量保存图片信息到数据库
                if (!venueImages.isEmpty()) {
                    boolean imageSaveSuccess = venueImageService.saveBatch(venueImages);
                    if (!imageSaveSuccess) {
                        // 图片保存失败，删除已上传的图片文件
                        for (String imageName : savedImageNames) {
                            File file = new File(uploadPath + "/" + imageName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        
                        // 删除刚创建的场地记录
                        venueService.removeById(venue.getId());
                        return Result.error("场地图片保存失败");
                    }
                }
            }
            
            // 查询完整的场地信息（包含图片）并返回
            VenueDto completeVenue = venueService.getVenueWithImagesById(venue.getId());
            redisComponent.setVenue(completeVenue);
            redisComponent.deleteVenueList();
            return Result.success(completeVenue);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result listVenue() {
        List<VenueDto> venues = redisComponent.getVenueList();
        if(venues==null) {
            venues = venueService.listVenuesWithImages();
            redisComponent.setVenueList(venues);
        }
        return Result.success(venues);
    }

    @GetMapping("/detail/{id}")
    public Result getVenueDetail(@PathVariable Integer id) {
        VenueDto venueDto = redisComponent.getVenue(id);
        if(venueDto==null){
            venueDto = venueService.getVenueWithImagesById(id);
            redisComponent.setVenue(venueDto);
        }
        if (venueDto == null) {
            return Result.error("场馆不存在");
        }
        return Result.success(venueDto);
    }

    @PostMapping("/update")
    public Result updateVenue(@NotNull Integer id,
                              String venueName,
                              String location,
                              BigDecimal price,
                              Integer capacity,
                              String introduction,
                              Integer[] tagIds,
                              String imageDtosJson,
                              @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            // 添加调试信息
            System.out.println("更新场馆 - 接收到参数:");
            System.out.println("ID: " + id);
            System.out.println("文件数组是否为null: " + (files == null));
            if (files != null) {
                System.out.println("文件数量: " + files.length);
                for (int i = 0; i < files.length; i++) {
                    System.out.println("文件 #" + i + " 是否为空: " + (files[i] == null || files[i].isEmpty()));
                    if (files[i] != null && !files[i].isEmpty()) {
                        System.out.println("文件 #" + i + " 名称: " + files[i].getOriginalFilename() + ", 大小: " + files[i].getSize());
                    }
                }
            }
            System.out.println("imageDtosJson: " + imageDtosJson);
            
            // 确保明确传递文件参数
            VenueDto venueDto = venueService.updateVenueWithImages(
                    id, venueName, location, price, capacity, introduction, tagIds, imageDtosJson, files);
            redisComponent.setVenue(venueDto);
            redisComponent.deleteVenueList();
            return Result.success(venueDto);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新场馆失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result deleteVenue(@RequestParam Integer id) {
        // 删除场馆及其所有图片
        boolean success = venueService.deleteVenueWithImages(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }


}

