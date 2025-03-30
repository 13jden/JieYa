package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.mapper.PropMapper;
import com.example.admin.mapper.PropRentalMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.service.PropRentalService;
import com.example.common.WxDto.PropRentalDto;
import com.example.common.pojo.Prop;
import com.example.common.pojo.PropImage;
import com.example.common.pojo.PropRental;
import com.example.common.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
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
public class PropRentalServiceImpl extends ServiceImpl<PropRentalMapper, PropRental> implements PropRentalService {

    @Autowired
    private PropMapper propMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PropRentalMapper propRentalMapper;
    
    @Value("${host.url}")
    private String hostUrl;
    
    @Override
    public Page<PropRentalDto> pageRentals(Integer current, Integer size, String userId, Integer propId, String status) {
        // 1. 构建查询条件
        QueryWrapper<PropRental> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(userId)) {
            queryWrapper.eq("user_id", userId);
        }
        if (propId != null) {
            queryWrapper.eq("prop_id", propId);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");
        
        // 2. 分页查询
        Page<PropRental> page = new Page<>(current, size);
        page = this.page(page, queryWrapper);
        
        // 3. 查询相关道具信息
        List<PropRental> rentals = page.getRecords();
        Page<PropRentalDto> dtoPage = new Page<>(
                page.getCurrent(), page.getSize(), page.getTotal());
        
        if (rentals.isEmpty()) {
            dtoPage.setRecords(new ArrayList<>());
            return dtoPage;
        }
        
        // 4. 获取相关道具ID
        Set<Integer> propIds = rentals.stream()
                .map(PropRental::getPropId)
                .collect(Collectors.toSet());
        
        // 5. 批量查询道具信息
        Map<Integer, Prop> propMap = new HashMap<>();
        if (!propIds.isEmpty()) {
            List<Prop> props = propMapper.selectBatchIds(propIds);
            propMap = props.stream()
                    .collect(Collectors.toMap(Prop::getId, prop -> prop));
        }
        
        // 6. 查询道具封面图
        Map<Integer, String> propCoverMap = getPropCoverImages(propIds);
        
        // 7. 转换为DTO对象
        Map<Integer, Prop> finalPropMap = propMap;
        List<PropRentalDto> dtoList = rentals.stream()
                .map(rental -> {
                    Prop prop = finalPropMap.getOrDefault(rental.getPropId(), new Prop());
                    String coverImage = propCoverMap.getOrDefault(rental.getPropId(), "");
                    
                    return PropRentalDto.fromPropRental(
                            rental,
                            prop.getName(),
                            prop.getDescription(),
                            prop.getPrice(),
                            coverImage
                    );
                })
                .collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public PropRentalDto getRentalDetailById(String id) {
        // 1. 查询租借记录
        PropRental rental = this.getById(id);
        if (rental == null) {
            return null;
        }
        
        // 2. 查询道具信息
        Prop prop = propMapper.selectById(rental.getPropId());
        if (prop == null) {
            prop = new Prop();
        }
        
        // 3. 查询道具封面图 - 使用新的Mapper方法
        PropImage coverImage = propMapper.findCoverImageByPropId(rental.getPropId());
        String imageUrl = "";
        if (coverImage != null) {
            imageUrl = hostUrl + "/images/prop/" + coverImage.getImageUrl();
        }
        
        // 4. 转换为DTO
        return PropRentalDto.fromPropRental(
            rental,
            prop.getName(),
            prop.getDescription(),
            prop.getPrice(),
            imageUrl
        );
    }

    @Override
    public List<PropRentalDto> getRentalsByUserId(String userId) {
        // 1. 查询用户的所有租借记录
        QueryWrapper<PropRental> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                  .orderByDesc("created_at");
        
        List<PropRental> rentals = this.list(queryWrapper);
        if (rentals.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 获取相关道具ID
        Set<Integer> propIds = rentals.stream()
                .map(PropRental::getPropId)
                .collect(Collectors.toSet());
        
        // 3. 批量查询道具信息
        Map<Integer, Prop> propMap = new HashMap<>();
        if (!propIds.isEmpty()) {
            List<Prop> props = propMapper.selectBatchIds(propIds);
            propMap = props.stream()
                    .collect(Collectors.toMap(Prop::getId, prop -> prop));
        }
        
        // 4. 查询道具封面图
        Map<Integer, String> propCoverMap = getPropCoverImages(propIds);
        
        // 5. 转换为DTO对象
        Map<Integer, Prop> finalPropMap = propMap;
        return rentals.stream()
                .map(rental -> {
                    Prop prop = finalPropMap.getOrDefault(rental.getPropId(), new Prop());
                    String coverImage = propCoverMap.getOrDefault(rental.getPropId(), "");
                    
                    return PropRentalDto.fromPropRental(
                            rental,
                            prop.getName(),
                            prop.getDescription(),
                            prop.getPrice(),
                            coverImage
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PropRentalDto> getRentalsByPropId(Integer propId) {
        // 使用新的Mapper方法直接获取所有租借记录
        List<PropRental> rentals = propRentalMapper.findAllRentalsByPropId(propId);
        if (rentals.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询道具信息
        Prop prop = propMapper.selectById(propId);
        if (prop == null) {
            prop = new Prop();
        }
        
        // 查询道具封面图
        PropImage coverImage = propMapper.findCoverImageByPropId(propId);
        String imageUrl = "";
        if (coverImage != null) {
            imageUrl = hostUrl + "/images/prop/" + coverImage.getImageUrl();
        }
        
        // 转换为DTO对象
        Prop finalProp = prop;
        String finalImageUrl = imageUrl;
        return rentals.stream()
            .map(rental -> PropRentalDto.fromPropRental(
                rental,
                finalProp.getName(),
                finalProp.getDescription(),
                finalProp.getPrice(),
                    finalImageUrl
            ))
            .collect(Collectors.toList());
    }

    @Override
    public boolean updateRentalStatus(String id, String status) {
        if (!StringUtils.hasText(id) || !StringUtils.hasText(status)) {
            return false;
        }
        
        UpdateWrapper<PropRental> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                   .set("status", status)
                   .set("updated_at", new Date());
        
        return this.update(updateWrapper);
    }
    
    /**
     * 获取道具封面图
     */
    private Map<Integer, String> getPropCoverImages(Set<Integer> propIds) {
        Map<Integer, String> coverImages = new HashMap<>();
        
        for (Integer propId : propIds) {
            // 此处应该查询道具的封面图，实现可能需要根据实际情况调整
            // 示例实现：查询排序为1的图片作为封面
            PropImage coverImage = propMapper.findCoverImageByPropId(propId);
            if (coverImage != null) {
                coverImages.put(propId, hostUrl + "/images/prop/" + coverImage.getImageUrl());
            }
        }
        
        return coverImages;
    }
}
