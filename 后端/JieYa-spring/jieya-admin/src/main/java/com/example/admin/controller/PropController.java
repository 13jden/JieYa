package com.example.admin.controller;

import com.alibaba.fastjson.JSON;
import com.example.admin.service.PropImageService;
import com.example.admin.service.PropService;
import com.example.common.adminDto.ImageDto;
import com.example.common.adminDto.PropDto;
import com.example.common.common.Result;
import com.example.common.pojo.Prop;
import com.example.common.pojo.PropImage;
import com.example.common.redis.RedisComponent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@RestController
@RequestMapping("/prop")
public class PropController {

    @Autowired
    private PropService propService;

    @Autowired
    private PropImageService propImageService;

    @Autowired
    private RedisComponent redisComponent;

    @Value("${upload.propFilePath}")
    private String uploadPath;

    @Value("${host.url}")
    private String hostUrl;

    @PostMapping("/add")
    public Result addProp(@NotEmpty String propName,
                         String description,
                         @NotNull BigDecimal price,
                         Boolean available,
                         String imageDtosJson,
                         MultipartFile[] files) {
        
        try {
            // 创建Prop对象
            Prop prop = new Prop();
            prop.setName(propName);
            prop.setDescription(description);
            prop.setPrice(price);
            prop.setAvailable(available != null ? available : true);
            prop.setCreatedAt(new Date());
            prop.setUpdatedAt(new Date());
            
            // 保存道具基本信息
            boolean success = propService.save(prop);
            if (!success) {
                return Result.error("道具添加失败");
            }

            // 处理图片文件
            if (files != null && files.length > 0) {
                // 解析imageDtosJson获取每张图片的信息
                ObjectMapper mapper = new ObjectMapper();
                List<ImageDto> imageDtos = mapper.readValue(imageDtosJson, 
                    mapper.getTypeFactory().constructCollectionType(List.class, ImageDto.class));
                
                if (imageDtos.size() != files.length) {
                    return Result.error("图片数量与图片信息不匹配");
                }
                
                // 处理每张图片
                List<PropImage> propImages = new ArrayList<>();
                List<String> savedImageNames = new ArrayList<>();
                
                // 确保目录存在
                File dir = new File(uploadPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    ImageDto imageDto = imageDtos.get(i);
                    
                    if (!file.isEmpty()) {
                        // 生成唯一的文件名
                        String originalFilename = file.getOriginalFilename();
                        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String fileName = System.currentTimeMillis() + suffix;
                        
                        // 保存文件
                        File dest = new File(uploadPath + "/" + fileName);
                        file.transferTo(dest);
                        
                        // 创建PropImage对象
                        PropImage propImage = new PropImage();
                        propImage.setPropId(prop.getId());
                        propImage.setImageUrl(fileName);
                        propImage.setSortOrder(imageDto.getSortOrder());
                        
                        // 添加调试信息
                        System.out.println("图片索引 " + i + ", isDetail传入值: " + imageDto.getIsDetail());
                        boolean isDetailValue = "1".equals(imageDto.getIsDetail());
                        System.out.println("转换后的Boolean值: " + isDetailValue);
                        
                        propImage.setIsDetail(isDetailValue);
                        propImage.setCreatedAt(new Date());
                        
                        // 再次检查设置后的值
                        System.out.println("设置后的isDetail值: " + propImage.getIsDetail());
                        
                        propImages.add(propImage);
                        savedImageNames.add(fileName);
                        
                        // 防止文件名重复
                        Thread.sleep(1);
                    }
                }
                
                // 批量保存图片信息到数据库
                if (!propImages.isEmpty()) {
                    boolean imageSaveSuccess = propImageService.saveBatch(propImages);
                    if (!imageSaveSuccess) {
                        // 图片保存失败，删除已上传的图片文件
                        for (String imageName : savedImageNames) {
                            File file = new File(uploadPath + "/" + imageName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        
                        // 删除刚创建的道具记录
                        propService.removeById(prop.getId());
                        return Result.error("道具图片保存失败");
                    }
                }
            }
            
            // 查询完整的道具信息（包含图片）并返回
            PropDto completeProp = propService.getPropWithImagesById(prop.getId());
            redisComponent.deletePropList();
            redisComponent.setProp(completeProp);
            return Result.success(completeProp);
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result listProps() {
        List<PropDto> props = redisComponent.getPropList();
        if(props==null){
            props = propService.listPropsWithImages();
            redisComponent.setPropList(props);
        }

        return Result.success(props);
    }

    @GetMapping("/detail/{id}")
    public Result getPropDetail(@PathVariable Integer id) {

        PropDto propDto = redisComponent.getProp(id);
        if (propDto == null) {
            propDto = propService.getPropWithImagesById(id);
            redisComponent.setProp(propDto);
        }
        if(propDto==null)
            return Result.error("道具不存在");

        return Result.success(propDto);
    }

    @PostMapping("/update")
    public Result updateProp(@NotNull Integer id,
                            String propName,
                            String description,
                            BigDecimal price,
                            Boolean available,
                            String imageDtosJson,
                             MultipartFile[] files) {
        try {
            PropDto updatedProp = propService.updatePropWithImages(
                id, propName, description, price, available, imageDtosJson, files
            );
            redisComponent.deletePropList();
            redisComponent.setProp(updatedProp);
            return Result.success(updatedProp);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新道具失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result deleteProp(@RequestParam Integer id) {
        // 删除道具及其所有图片
        boolean success = propService.deletePropWithImages(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}

