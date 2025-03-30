package com.example.admin.service;

import com.example.common.pojo.VenueTag;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
public interface VenueTagService extends IService<VenueTag> {

    /**
     * 检查标签名是否存在
     */
    boolean existsByName(String tagName);

    /**
     * 检查标签是否被使用
     */
    boolean isTagInUse(Integer tagId);

    /**
     * 保存场馆和标签的关联
     */
    void saveVenueTag(Integer venueId, Integer[] tagIds);
}
