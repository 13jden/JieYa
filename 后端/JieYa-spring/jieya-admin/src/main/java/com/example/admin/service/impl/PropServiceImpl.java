package com.example.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.admin.mapper.PropImageMapper;
import com.example.admin.mapper.PropMapper;
import com.example.admin.service.PropImageService;
import com.example.admin.service.PropService;
import com.example.common.adminDto.ImageDto;
import com.example.common.adminDto.PropDto;
import com.example.common.pojo.Prop;
import com.example.common.pojo.PropImage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.redis.RedisComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Service
public class PropServiceImpl extends ServiceImpl<PropMapper, Prop> implements PropService {

    @Autowired
    private PropImageMapper propImageMapper;
    
    @Autowired
    private PropImageService propImageService;
    
    @Value("${upload.propFilePath}")
    private String uploadPath;
    
    @Value("${host.url}")
    private String hostUrl;

    @Autowired
    private RedisComponent redisComponent;

    @Override
    public PropDto getPropWithImagesById(Integer id) {
        // 查询道具基本信息
        Prop prop = this.getById(id);
        if (prop == null) {
            return null;
        }
        
        // 创建DTO并复制基本属性
        PropDto propDto = new PropDto();
        BeanUtils.copyProperties(prop, propDto);
        
        // 查询道具的所有普通图片，按sortOrder排序
        QueryWrapper<PropImage> normalImagesQuery = new QueryWrapper<>();
        normalImagesQuery.eq("prop_id", id)
                       .eq("is_detail", false)
                       .orderByAsc("sort_order");
        List<PropImage> normalImages = propImageMapper.selectList(normalImagesQuery);

        // 查询道具的所有详情图片，按sortOrder排序
        QueryWrapper<PropImage> detailImagesQuery = new QueryWrapper<>();
        detailImagesQuery.eq("prop_id", id)
                        .eq("is_detail", true)
                        .orderByAsc("sort_order");
        List<PropImage> detailImages = propImageMapper.selectList(detailImagesQuery);
        
        // 转换普通图片列表
        List<ImageDto> normalImageDtoList = normalImages.stream()
            .map(image -> {
                ImageDto imageDto = new ImageDto();
                imageDto.setId(image.getId());
                imageDto.setImageUrl(hostUrl + "/images/prop/" + image.getImageUrl());
                imageDto.setSortOrder(image.getSortOrder());
                imageDto.setIsDetail("0"); 
                return imageDto;
            })
            .collect(Collectors.toList());
        propDto.setImageList(normalImageDtoList);
        
        // 转换详情图片列表
        List<ImageDto> detailImageDtoList = detailImages.stream()
            .map(image -> {
                ImageDto imageDto = new ImageDto();
                imageDto.setId(image.getId());
                imageDto.setImageUrl(hostUrl + "/images/prop/" + image.getImageUrl());
                imageDto.setSortOrder(image.getSortOrder());
                imageDto.setIsDetail("1");
                return imageDto;
            })
            .collect(Collectors.toList());
        propDto.setDetailImageList(detailImageDtoList);
        
        // 设置封面图片(正常图片中的第一张)
        if (!normalImages.isEmpty()) {
            PropImage coverImage = normalImages.get(0);
            propDto.setCoverImage(hostUrl + "/images/prop/" + coverImage.getImageUrl());
        }
        
        return propDto;
    }

    @Override
    public List<PropDto> listPropsWithImages() {
        // 查询所有道具
        List<Prop> props = this.list();
        List<PropDto> propDtoList = new ArrayList<>();
        
        if (props != null && !props.isEmpty()) {
            for (Prop prop : props) {
                // 查询每个道具的所有图片
                PropDto dto = getPropWithImagesById(prop.getId());
                propDtoList.add(dto);
            }
        }
        return propDtoList;
    }
    
    @Override
    @Transactional
    public boolean deletePropWithImages(Integer id) {
        try {
            // 查询该道具所有图片
            QueryWrapper<PropImage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("prop_id", id);
            List<PropImage> images = propImageMapper.selectList(queryWrapper);
            
            // 删除物理文件
            if (images != null && !images.isEmpty()) {
                for (PropImage image : images) {
                    File file = new File(uploadPath + "/" + image.getImageUrl());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                
                // 删除数据库记录
                propImageMapper.delete(queryWrapper);
                redisComponent.deleteProp(id);
            }
            
            // 删除道具记录
            return this.removeById(id);
        } catch (Exception e) {
            // 异常时回滚事务
            throw new RuntimeException("删除道具及图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PropDto updatePropWithImages(Integer id, String propName, String description,
                                      BigDecimal price, Boolean available,
                                      String imageDtosJson, MultipartFile[] newFiles) throws Exception {
        // 1. 更新道具基本信息
        Prop prop = this.getById(id);
        if (prop == null) {
            throw new RuntimeException("道具不存在");
        }

        if (propName != null) prop.setName(propName);
        if (description != null) prop.setDescription(description);
        if (price != null) prop.setPrice(price);
        if (available != null) prop.setAvailable(available);
        
        prop.setUpdatedAt(new Date());
        this.updateById(prop);

        // 2. 处理图片更新
        if (imageDtosJson != null) {
            // 添加调试日志
            System.out.println("接收到的imageDtosJson: " + imageDtosJson);
            if (newFiles != null) {
                System.out.println("上传的文件数量: " + newFiles.length);
            }
            
            ObjectMapper mapper = new ObjectMapper();
            List<ImageDto> imageDtos = mapper.readValue(imageDtosJson, 
                    mapper.getTypeFactory().constructCollectionType(List.class, ImageDto.class));
            
            // 收集前端传回的图片ID列表
            Set<Integer> remainingImageIds = new HashSet<>();
            List<ImageDto> existingImageDtos = new ArrayList<>();
            List<ImageDto> newImageDtos = new ArrayList<>();
            
            // 将图片DTO分为已有图片和新图片两组
            for (ImageDto dto : imageDtos) {
                System.out.println("处理图片DTO: " + mapper.writeValueAsString(dto));
                if (dto.getId() != null) {
                    remainingImageIds.add(dto.getId());
                    existingImageDtos.add(dto);
                } else {
                    newImageDtos.add(dto);
                }
            }
            
            System.out.println("已有图片数: " + existingImageDtos.size());
            System.out.println("新图片数: " + newImageDtos.size());
            
            // 查询当前所有图片
            QueryWrapper<PropImage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("prop_id", id);
            List<PropImage> currentImages = propImageService.list(queryWrapper);
            
            // 找出需要删除的图片
            List<PropImage> imagesToDelete = new ArrayList<>();
            for (PropImage image : currentImages) {
                if (!remainingImageIds.contains(image.getId())) {
                    imagesToDelete.add(image);
                }
            }
            
            // 删除不再使用的图片
            for (PropImage image : imagesToDelete) {
                // 删除物理文件
                File file = new File(uploadPath + "/" + image.getImageUrl());
                if (file.exists()) {
                    file.delete();
                }
                // 删除数据库记录
                propImageService.removeById(image.getId());
            }
            
            // 处理已有图片的更新
            for (ImageDto imageDto : existingImageDtos) {
                PropImage existingImage = propImageService.getById(imageDto.getId());
                if (existingImage != null) {
                    existingImage.setSortOrder(imageDto.getSortOrder());
                    existingImage.setIsDetail("1".equals(imageDto.getIsDetail()));
                    propImageService.updateById(existingImage);
                }
            }
            
            // 处理新增图片 - 修改这部分逻辑，添加更多调试信息
            System.out.println("newFiles是否为null: " + (newFiles == null));
            if (newFiles != null) {
                System.out.println("newFiles长度: " + newFiles.length);
                for (int i = 0; i < newFiles.length; i++) {
                    System.out.println("文件 #" + i + " 是否为空: " + (newFiles[i] == null || newFiles[i].isEmpty()));
                    if (newFiles[i] != null) {
                        System.out.println("文件 #" + i + " 名称: " + newFiles[i].getOriginalFilename());
                        System.out.println("文件 #" + i + " 大小: " + newFiles[i].getSize());
                    }
                }
            }
            
            if (newFiles != null && newFiles.length > 0 && !newImageDtos.isEmpty()) {
                // 如果新图片DTO数量与文件数量不匹配，输出警告但继续处理
                if (newImageDtos.size() != newFiles.length) {
                    System.out.println("警告: 新图片信息数量(" + newImageDtos.size() + 
                                      ")与上传文件数量(" + newFiles.length + ")不匹配");
                }
                
                // 取较小的值作为循环次数
                int processCount = Math.min(newFiles.length, newImageDtos.size());
                System.out.println("将处理 " + processCount + " 个新图片");
                
                for (int i = 0; i < processCount; i++) {
                    MultipartFile file = newFiles[i];
                    ImageDto imageDto = newImageDtos.get(i);
                    
                    if (file == null || file.isEmpty()) {
                        System.out.println("警告: 文件 #" + i + " 为空，跳过处理");
                        continue;
                    }
                    
                    try {
                        // 生成唯一的文件名
                        String originalFilename = file.getOriginalFilename();
                        System.out.println("处理文件: " + originalFilename + ", 大小: " + file.getSize());
                        
                        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String fileName = System.currentTimeMillis() + suffix;
                        
                        // 保存文件
                        File dest = new File(uploadPath + "/" + fileName);
                        System.out.println("保存到路径: " + dest.getAbsolutePath());
                        
                        file.transferTo(dest);
                        
                        // 创建新图片记录
                        PropImage newImage = new PropImage();
                        newImage.setPropId(id);
                        newImage.setImageUrl(fileName);
                        newImage.setSortOrder(imageDto.getSortOrder());
                        newImage.setIsDetail("1".equals(imageDto.getIsDetail()));
                        newImage.setCreatedAt(new Date());
                        
                        boolean saved = propImageService.save(newImage);
                        System.out.println("新图片保存结果: " + saved + ", 图片ID: " + newImage.getId());
                        
                        // 防止文件名重复
                        Thread.sleep(1);
                    } catch (Exception e) {
                        System.err.println("处理新图片时出错: " + e.getMessage());
                        e.printStackTrace();
                        throw e; // 重新抛出异常，以便查看完整错误
                    }
                }
            } else {
                System.out.println("没有新图片需要处理 (文件: " + 
                                 (newFiles == null ? "null" : newFiles.length) + 
                                 ", DTOs: " + newImageDtos.size() + ")");
                
                if (newFiles == null || newFiles.length == 0) {
                    System.out.println("警告: 没有上传文件，但有" + newImageDtos.size() + "个新图片DTO");
                }
            }
        }
        
        // 返回更新后的道具信息
        return this.getPropWithImagesById(id);
    }
}
