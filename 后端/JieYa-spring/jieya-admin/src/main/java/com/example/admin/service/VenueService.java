package com.example.admin.service;

import com.example.common.adminDto.VenueDto;
import com.example.common.pojo.Venue;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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

    VenueDto getVenueWithImagesById(Integer id);

    List<VenueDto> listVenuesWithImages();

    List<VenueDto> listVenuesWithCoverImages();

    boolean deleteVenueWithImages(Integer id);

    /**
     * 更新场馆及其图片
     * @param id 场馆ID
     * @param venueName 场馆名称
     * @param location 位置
     * @param price 价格
     * @param capacity 容量
     * @param introduction 介绍内容
     * @param tagIds 标签ID数组
     * @param imageDtosJson 图片DTO JSON字符串
     * @param newFiles 新上传的文件
     * @return 更新后的场馆DTO
     */
    VenueDto updateVenueWithImages(Integer id, String venueName, String location, 
                                  BigDecimal price, Integer capacity, String introduction,
                                  Integer[] tagIds, String imageDtosJson, MultipartFile[] newFiles) throws Exception;
}
