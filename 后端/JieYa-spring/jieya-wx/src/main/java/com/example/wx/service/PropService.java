package com.example.wx.service;

import com.example.common.adminDto.PropDto;
import com.example.common.pojo.Prop;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
public interface PropService extends IService<Prop> {
        
    /**
     * 获取道具详情（包含所有图片）
     */
    PropDto getPropWithImagesById(Integer id);
    
    /**
     * 获取所有道具列表（仅包含封面图）
     */
    List<PropDto> listPropsWithCoverImages();
    
    /**
     * 搜索道具
     */
    List<PropDto> searchProps(String keyword);
    
    /**
     * 获取可用道具列表
     */
    List<PropDto> listAvailableProps();

    /**
     * 获取道具封面图
     */
    Map<Integer, String> getPropCoverImages(List<Integer> propIds);

}
