package com.example.wx.service;

import com.example.common.adminDto.VenueDto;
import com.example.common.pojo.Venue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
public interface VenueService extends IService<Venue> {

      /**
     * 获取场地详情（包含所有图片）
     */
    VenueDto getVenueWithImagesById(Integer id);
    
    /**
     * 获取所有场地列表（仅包含封面图）
     */
    List<VenueDto> listVenuesWithCoverImages();
    
    /**
     * 根据标签ID获取场地列表
     */
    List<VenueDto> listVenuesByTagId(Integer tagId);
    
    /**
     * 搜索场地
     */
    List<VenueDto> searchVenues(String keyword);

}
