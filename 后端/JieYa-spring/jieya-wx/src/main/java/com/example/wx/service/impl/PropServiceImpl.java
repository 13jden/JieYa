package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.adminDto.ImageDto;
import com.example.common.adminDto.PropDto;
import com.example.common.pojo.Prop;
import com.example.common.pojo.PropImage;
import com.example.common.redis.RedisComponent;
import com.example.wx.mapper.PropImageMapper;
import com.example.wx.mapper.PropMapper;
import com.example.wx.service.PropService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private RedisComponent redisComponent;
    
    @Value("${host.url}")
    private String hostUrl;


    @Override
    public PropDto getPropWithImagesById(Integer id) {
        // 1. 获取道具基本信息
        Prop prop = this.getById(id);
        if (prop == null) {
            return null;
        }
        
        // 2. 转换为DTO
        PropDto propDto = convertToDto(prop);
        
        // 3. 获取并设置所有图片
        QueryWrapper<PropImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("prop_id", id);
        queryWrapper.orderByAsc("sort_order");
        List<PropImage> images = propImageMapper.selectList(queryWrapper);
        
        if (!images.isEmpty()) {
            // 分离普通图片和详情图片
            List<PropImage> normalImages = images.stream()
                .filter(img -> !img.getIsDetail())
                .collect(Collectors.toList());
            
            List<PropImage> detailImages = images.stream()
                .filter(PropImage::getIsDetail)
                .collect(Collectors.toList());
            
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
            
            propDto.setImageList(normalImageDtoList);
            propDto.setDetailImageList(detailImageDtoList);
            
            // 设置封面图（取第一张普通图片作为封面）
            if (!normalImageDtoList.isEmpty()) {
                propDto.setCoverImage(normalImageDtoList.get(0).getImageUrl());
            } else if (!detailImageDtoList.isEmpty()) {
                propDto.setCoverImage(detailImageDtoList.get(0).getImageUrl());
            }
        }
        
        return propDto;
    }

    @Override
    public List<PropDto> listPropsWithCoverImages() {
        // 1. 获取所有道具
        List<Prop> props = this.list();
        if (props.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 转换为DTO列表
        List<PropDto> propDtos = props.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        // 3. 为每个道具设置封面图
        for (PropDto propDto : propDtos) {
            setCoverImage(propDto);
        }
        
        return propDtos;
    }

    @Override
    public List<PropDto> searchProps(String keyword) {
        // 1. 根据关键词搜索道具
        QueryWrapper<Prop> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", keyword)
            .or()
            .like("description", keyword);
        
        List<Prop> props = this.list(queryWrapper);
        if (props.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 转换为DTO列表
        List<PropDto> propDtos = props.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        // 3. 为每个道具设置封面图
        for (PropDto propDto : propDtos) {
            setCoverImage(propDto);
        }
        
        return propDtos;
    }

    @Override
    public List<PropDto> listAvailableProps() {
        QueryWrapper<Prop> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("available", true);

        List<Prop> props = this.list(queryWrapper);
        if (props.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 转换为DTO列表
        List<PropDto> propDtos = props.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        // 3. 为每个道具设置封面图
        for (PropDto propDto : propDtos) {
            setCoverImage(propDto);
        }
        
        return propDtos;
    }
    
    // 辅助方法：将Prop转换为PropDto
    private PropDto convertToDto(Prop prop) {
        PropDto propDto = new PropDto();
        propDto.setId(prop.getId());
        propDto.setName(prop.getName());
        propDto.setDescription(prop.getDescription());
        propDto.setPrice(prop.getPrice());
        propDto.setAvailable(prop.getAvailable());
        propDto.setCreatedAt(prop.getCreatedAt());
        propDto.setUpdatedAt(prop.getUpdatedAt());
        
        return propDto;
    }
    
    // 辅助方法：设置道具封面图
    private void setCoverImage(PropDto propDto) {
        // 先尝试获取普通图片作为封面
        QueryWrapper<PropImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("prop_id", propDto.getId());
        queryWrapper.eq("is_detail", false);
        queryWrapper.orderByAsc("sort_order");
        queryWrapper.last("LIMIT 1");
        
        PropImage coverImage = propImageMapper.selectOne(queryWrapper);
        
        // 如果没有普通图片，则获取详情图片作为封面
        if (coverImage == null) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("prop_id", propDto.getId());
            queryWrapper.eq("is_detail", true);
            queryWrapper.orderByAsc("sort_order");
            queryWrapper.last("LIMIT 1");
            
            coverImage = propImageMapper.selectOne(queryWrapper);
        }
        
        if (coverImage != null) {
            propDto.setCoverImage(hostUrl + "/images/prop/" + coverImage.getImageUrl());
        }
    }

    @Override
    public Map<Integer, String> getPropCoverImages(List<Integer> propIds) {
        if (propIds == null || propIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 查询所有指定道具的图片
        QueryWrapper<PropImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("prop_id", propIds)
                  .eq("is_detail", "0")  // 非详情图片
                  .orderBy(true, true, "sort_order");  // 按排序字段升序排列
        
        List<PropImage> images = propImageMapper.selectList(queryWrapper);
        
        // 为每个道具选取第一张图片作为封面
        Map<Integer, String> result = new HashMap<>();
        Map<Integer, List<PropImage>> groupedImages = images.stream()
                .collect(Collectors.groupingBy(PropImage::getPropId));
        
        for (Map.Entry<Integer, List<PropImage>> entry : groupedImages.entrySet()) {
            Integer propId = entry.getKey();
            List<PropImage> propImages = entry.getValue();
            if (!propImages.isEmpty()) {
                PropImage coverImage = propImages.get(0);
                String imageUrl = hostUrl + "/images/prop/" + coverImage.getImageUrl();
                result.put(propId, imageUrl);
            }
        }
        
        return result;
    }
}
