package com.example.wx.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.adminDto.ImageDto;
import com.example.common.adminDto.VenueDto;
import com.example.common.pojo.Venue;
import com.example.common.pojo.VenueImage;
import com.example.common.redis.RedisComponent;
import com.example.wx.mapper.VenueImageMapper;
import com.example.wx.mapper.VenueMapper;
import com.example.wx.service.VenueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    
    @Value("${host.url}")
    private String hostUrl;

    @Override
    public VenueDto getVenueWithImagesById(Integer id) {
        // 1. 先从Redis获取
        VenueDto venueDto = redisComponent.getVenue(id);
        if (venueDto != null) {
            return venueDto;
        }
        
        // 2. Redis没有，从数据库获取
        Venue venue = this.getById(id);
        if (venue == null) {
            return null;
        }
        
        // 3. 转换为DTO
        venueDto = convertToDto(venue);
        
        // 4. 获取并设置所有图片
        QueryWrapper<VenueImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("venue_id", id);
        queryWrapper.orderByAsc("sort_order");
        List<VenueImage> images = venueImageMapper.selectList(queryWrapper);
        
        if (!images.isEmpty()) {
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
            
            // 设置封面图（取第一张图片作为封面）
            venueDto.setCoverImage(imageDtoList.get(0).getImageUrl());
        }
        
        // 5. 存入Redis
        redisComponent.setVenue(venueDto);
        
        return venueDto;
    }

    @Override
    public List<VenueDto> listVenuesWithCoverImages() {
        // 1. 先从Redis获取
        List<VenueDto> venueDtos = redisComponent.getVenueList();
        if (venueDtos != null && !venueDtos.isEmpty()) {
            // 为每个场地设置封面图
            for (VenueDto venueDto : venueDtos) {
                setCoverImage(venueDto);
            }
            return venueDtos;
        }
        
        // 2. Redis没有，从数据库获取
        List<Venue> venues = this.list();
        if (venues.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 3. 转换为DTO列表
        venueDtos = venues.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        redisComponent.setVenueList(venueDtos);
        // 4. 为每个场地设置封面图
        for (VenueDto venueDto : venueDtos) {
            setCoverImage(venueDto);
        }
        return venueDtos;
    }

    @Override
    public List<VenueDto> listVenuesByTagId(Integer tagId) {

        List<Venue> allVenues = this.list();
        
        // 2. 过滤出包含指定标签的场地
        List<Venue> filteredVenues = allVenues.stream()
            .filter(venue -> {
                if (venue.getTags() == null || venue.getTags().isEmpty()) {
                    return false;
                }
                
                JSONArray tagsArray = JSON.parseArray(venue.getTags());
                for (int i = 0; i < tagsArray.size(); i++) {
                    if (tagId.equals(tagsArray.getInteger(i))) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
        
        // 3. 转换为DTO列表
        List<VenueDto> venueDtos = filteredVenues.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        // 4. 为每个场地设置封面图
        for (VenueDto venueDto : venueDtos) {
            setCoverImage(venueDto);
        }
        
        return venueDtos;
    }

    @Override
    public List<VenueDto> searchVenues(String keyword) {
        // 1. 根据关键词搜索场地
        QueryWrapper<Venue> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", keyword)
            .or()
            .like("content", keyword)
            .or()
            .like("location", keyword);
        
        List<Venue> venues = this.list(queryWrapper);
        if (venues.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 转换为DTO列表
        List<VenueDto> venueDtos = venues.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        // 3. 为每个场地设置封面图
        for (VenueDto venueDto : venueDtos) {
            setCoverImage(venueDto);
        }
        
        return venueDtos;
    }
    
    // 辅助方法：将Venue转换为VenueDto
    private VenueDto convertToDto(Venue venue) {
        VenueDto venueDto = new VenueDto();
        venueDto.setId(venue.getId());
        venueDto.setName(venue.getName());
        venueDto.setContent(venue.getContent());
        venueDto.setLocation(venue.getLocation());
        venueDto.setPrice(venue.getPrice());
        venueDto.setCapacity(venue.getCapacity());
        venueDto.setCreatedAt(venue.getCreatedAt());
        venueDto.setUpdatedAt(venue.getUpdatedAt());
        
        // 处理标签
        if (venue.getTags() != null && !venue.getTags().isEmpty()) {
            venueDto.setTags(JSON.parseArray(venue.getTags()));
        }
        
        return venueDto;
    }
    
    // 辅助方法：设置场地封面图
    private void setCoverImage(VenueDto venueDto) {
        QueryWrapper<VenueImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("venue_id", venueDto.getId());
        queryWrapper.orderByAsc("sort_order");
        queryWrapper.last("LIMIT 1");
        
        VenueImage coverImage = venueImageMapper.selectOne(queryWrapper);
        if (coverImage != null) {
            venueDto.setCoverImage(hostUrl + "/images/venue/" + coverImage.getImageUrl());
        }
    }
}
