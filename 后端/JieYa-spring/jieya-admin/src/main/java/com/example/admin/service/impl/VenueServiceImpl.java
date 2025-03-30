package com.example.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.admin.mapper.VenueImageMapper;
import com.example.admin.mapper.VenueMapper;
import com.example.admin.service.VenueImageService;
import com.example.admin.service.VenueService;
import com.example.common.adminDto.ImageDto;
import com.example.common.adminDto.VenueDto;
import com.example.common.pojo.Venue;
import com.example.common.pojo.VenueImage;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class VenueServiceImpl extends ServiceImpl<VenueMapper, Venue> implements VenueService {

    @Autowired
    private VenueImageMapper venueImageMapper;

    @Autowired
    private RedisComponent redisComponent;
    
    @Autowired
    private VenueImageService venueImageService;
    
    @Value("${upload.venueFilePath}")
    private String uploadPath;
    @Value("${host.url}")
    private String hostUrl;

    @Override
    public VenueDto getVenueWithImagesById(Integer id) {
        // 查询场馆基本信息
        Venue venue = this.getById(id);
        if (venue == null) {
            return null;
        }
        
        // 创建DTO并复制基本属性
        VenueDto venueDto = new VenueDto();
        BeanUtils.copyProperties(venue, venueDto);

        if (venue.getTags() != null && !venue.getTags().isEmpty()) {
            try {
                // 将JSON字符串解析为Integer数组并设置到DTO中
                JSONArray tagsArray = JSON.parseArray(venue.getTags());
                venueDto.setTags(tagsArray);
            } catch (Exception e) {
                System.err.println("解析tags出错: " + e.getMessage());
                venueDto.setTags(new JSONArray());
            }
        } else {
            venueDto.setTags(new JSONArray());
        }
        
        // 查询场馆的所有图片，按sortOrder排序
        QueryWrapper<VenueImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("venue_id", id).orderByAsc("sort_order");
        List<VenueImage> images = venueImageMapper.selectList(queryWrapper);

        List<ImageDto> imageDtoList = images.stream()
            .map(image -> {
                ImageDto imageDto = new ImageDto();
                imageDto.setId(image.getId());
                imageDto.setImageUrl(hostUrl + "/images/venue/" + image.getImageUrl());
                imageDto.setSortOrder(image.getSortOrder());
                return imageDto;
            })
            .collect(Collectors.toList());
        venueDto.setImageList(imageDtoList);
        
        // 直接查询封面图片(sortOrder=1)
        VenueImage coverImage = venueImageMapper.findByVenueIdAndSortOrder(id, 1);
        if (coverImage != null) {
            venueDto.setCoverImage(hostUrl + "/images/venue/" + coverImage.getImageUrl());
        }
        
        return venueDto;
    }

    @Override
    public List<VenueDto> listVenuesWithImages() {
        // 查询所有场馆
        List<Venue> venues = this.list();
        List<VenueDto> venueDtoList = new ArrayList<>();
        
        if (venues != null && !venues.isEmpty()) {
            for (Venue venue : venues) {
                // 查询每个场馆的所有图片
                VenueDto dto = getVenueWithImagesById(venue.getId());
                venueDtoList.add(dto);
            }
        }
        return venueDtoList;
    }

    @Override
    public List<VenueDto> listVenuesWithCoverImages() {
        // 查询所有场馆
        List<Venue> venues = this.list();
        List<VenueDto> venueDtoList = new ArrayList<>();
        
        if (venues != null && !venues.isEmpty()) {
            for (Venue venue : venues) {
                // 创建DTO并复制基本属性
                VenueDto venueDto = new VenueDto();
                BeanUtils.copyProperties(venue, venueDto);
                if (venue.getTags() != null && !venue.getTags().isEmpty()) {
                    try {
                        JSONArray tagsArray = JSON.parseArray(venue.getTags());
                        venueDto.setTags(tagsArray);
                    } catch (Exception e) {
                        System.err.println("解析tags出错: " + e.getMessage());
                        venueDto.setTags(new JSONArray());
                    }
                } else {
                    venueDto.setTags(new JSONArray());
                }
                
                // 直接查询封面图片
                VenueImage coverImage = venueImageMapper.findByVenueIdAndSortOrder(venue.getId(), 1);
                if (coverImage != null) {
                    venueDto.setCoverImage(hostUrl + "/images/venue/" + coverImage.getImageUrl());
                }
                
                venueDtoList.add(venueDto);
            }
        }
        
        return venueDtoList;
    }
    
    @Override
    @Transactional
    public boolean deleteVenueWithImages(Integer id) {
        try {
            // 查询该场馆所有图片
            QueryWrapper<VenueImage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("venue_id", id);
            List<VenueImage> images = venueImageMapper.selectList(queryWrapper);
            
            // 删除物理文件
            if (images != null && !images.isEmpty()) {
                for (VenueImage image : images) {
                    File file = new File(uploadPath+"/"+ image.getImageUrl());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                
                // 删除数据库记录
                venueImageMapper.delete(queryWrapper);
            }
            redisComponent.deleteVenueList();
            redisComponent.deleteVenue(id);
            // 删除场馆记录
            return this.removeById(id);
        } catch (Exception e) {
            // 异常时回滚事务
            throw new RuntimeException("删除场馆及图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueDto updateVenueWithImages(Integer id, String venueName, String location, 
                                        BigDecimal price, Integer capacity, String introduction,
                                        Integer[] tagIds, String imageDtosJson, MultipartFile[] newFiles) throws Exception {
        // 1. 更新场馆基本信息
        Venue venue = this.getById(id);
        if (venue == null) {
            throw new RuntimeException("场馆不存在");
        }
        
        venue.setName(venueName);
        venue.setLocation(location);
        venue.setPrice(price);
        venue.setCapacity(capacity);
        venue.setContent(introduction);
        
        // 处理标签
        if(tagIds != null && tagIds.length > 0) {
            venue.setTags(JSON.toJSONString(tagIds));
        } else {
            venue.setTags("[]");
        }
        
        venue.setUpdatedAt(new Date());
        this.updateById(venue);

        // 2. 处理图片更新
        if (imageDtosJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            List<ImageDto> imageDtos = mapper.readValue(imageDtosJson, 
                    mapper.getTypeFactory().constructCollectionType(List.class, ImageDto.class));
            
            // 查询当前所有图片
            QueryWrapper<VenueImage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("venue_id", id);
            List<VenueImage> currentImages = venueImageService.list(queryWrapper);
            
            // 获取现有图片URL到ID的映射
            Map<String, Integer> existingUrlToIdMap = new HashMap<>();
            for (VenueImage image : currentImages) {
                String fullUrl = hostUrl + "/images/venue/" + image.getImageUrl();
                existingUrlToIdMap.put(fullUrl, image.getId());
            }
            
            // 收集需要保留的图片ID
            Set<Integer> remainingImageIds = new HashSet<>();
            List<ImageDto> newImageDtos = new ArrayList<>();
            
            // 处理每个传入的图片DTO
            for (ImageDto dto : imageDtos) {
                if (dto.getId() != null) {
                    // 如果明确提供ID，直接使用
                    remainingImageIds.add(dto.getId());
                } else if (dto.getImageUrl() != null && !dto.getImageUrl().isEmpty() && 
                           existingUrlToIdMap.containsKey(dto.getImageUrl())) {
                    // 如果没有ID但有URL，通过URL查找已存在的图片
                    int existingId = existingUrlToIdMap.get(dto.getImageUrl());
                    remainingImageIds.add(existingId);
                    // 设置ID以便更新
                    dto.setId(existingId);
                } else {
                    // 新图片
                    newImageDtos.add(dto);
                }
            }
            
            // 找出需要删除的图片
            List<VenueImage> imagesToDelete = new ArrayList<>();
            for (VenueImage image : currentImages) {
                if (!remainingImageIds.contains(image.getId())) {
                    imagesToDelete.add(image);
                }
            }
            
            // 删除不再使用的图片
            for (VenueImage image : imagesToDelete) {
                // 删除物理文件
                File file = new File(uploadPath + "/" + image.getImageUrl());
                if (file.exists()) {
                    file.delete();
                }
                // 删除数据库记录
                venueImageService.removeById(image.getId());
            }
            
            // 处理已有图片的更新
            for (ImageDto imageDto : imageDtos) {
                if (imageDto.getId() != null) {
                    VenueImage existingImage = venueImageService.getById(imageDto.getId());
                    if (existingImage != null) {
                        existingImage.setSortOrder(imageDto.getSortOrder());
                        venueImageService.updateById(existingImage);
                    }
                }
            }
            
            // 处理新增图片
            if (newFiles != null && newFiles.length > 0 && !newImageDtos.isEmpty()) {
                // 取较小的值作为循环次数
                int processCount = Math.min(newFiles.length, newImageDtos.size());
                
                for (int i = 0; i < processCount; i++) {
                    MultipartFile file = newFiles[i];
                    ImageDto imageDto = newImageDtos.get(i);
                    
                    if (file == null || file.isEmpty()) {
                        continue;
                    }
                    
                    try {
                        // 生成唯一的文件名
                        String originalFilename = file.getOriginalFilename();
                        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String fileName = System.currentTimeMillis() + suffix;
                        
                        // 保存文件
                        File dest = new File(uploadPath + "/" + fileName);
                        file.transferTo(dest);
                        
                        // 创建新图片记录
                        VenueImage newImage = new VenueImage();
                        newImage.setVenueId(id);
                        newImage.setImageUrl(fileName);
                        newImage.setSortOrder(imageDto.getSortOrder());
                        newImage.setCreatedAt(new Date());
                        
                        venueImageService.save(newImage);
                        
                        // 防止文件名重复
                        Thread.sleep(1);
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
        
        // 返回更新后的场馆信息
        return this.getVenueWithImagesById(id);
    }
}
