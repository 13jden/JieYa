package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.pojo.Banner;
import com.example.common.pojo.Category;
import com.example.common.redis.RedisComponent;
import com.example.wx.mapper.BannerMapper;
import com.example.wx.mapper.CategoryMapper;
import com.example.wx.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Banner服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private RedisComponent redisComponent;

    /**
     * 获取Banner列表，按sort排序
     * @return Banner列表
     */
    @Override
    public List<Banner> getList() {
        // 先从缓存中获取
        List<Banner> banners = redisComponent.getBannerList();
        if (banners != null) {
            // 过滤掉on等于0的Banner
            banners = banners.stream()
                    .filter(banner -> banner.getOn() != null && banner.getOn() == 1)
                    .collect(Collectors.toList());
                    
            // 转换图片URL
            convertImageUrls(banners);
            return banners;
        }
        
        // 缓存中没有，从数据库获取
        // 使用自定义的查询方法，只获取启用的Banner
        banners = bannerMapper.selectEnabledList();
        
        // 存入缓存（存储原始数据，不包含URL前缀）
        redisComponent.setBannerList(banners);
        
        // 转换图片URL
        convertImageUrls(banners);
        
        return banners;
    }

    /**
     * 转换图片URL为完整访问路径
     * @param banners Banner列表
     */
    private void convertImageUrls(List<Banner> banners) {
        if (banners != null && !banners.isEmpty()) {
            for (Banner banner : banners) {
                if (banner.getImage() != null && !banner.getImage().isEmpty()) {
                    String imageUrl = banner.getImage();
                    if (!imageUrl.startsWith("http://")) {
                        String baseUrl = "http://localhost:8081/images/banner/";
                        banner.setImage(baseUrl + imageUrl);
                    }
                }
            }
        }
    }
}
