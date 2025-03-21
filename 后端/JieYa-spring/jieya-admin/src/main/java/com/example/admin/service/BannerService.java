package com.example.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.pojo.Banner;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Banner服务接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
public interface BannerService extends IService<Banner> {

    /**
     * 获取Banner列表，按sort排序
     * @return Banner列表
     */
    List<Banner> getList();

    /**
     * 添加Banner
     * @param image 图片
     * @param type 类型：1为场地，2为商品，3为笔记
     * @param on 是否启用：1是，0否
     * @param text 标题（可选）
     * @param contentId 跳转ID（可选）
     * @return 是否添加成功
     */
    boolean add(String image, Integer type, Integer on, String text, Integer contentId);

    /**
     * 更新Banner
     * @param bannerId Banner ID
     * @param image 图片
     * @param type 类型：1为场地，2为商品，3为笔记
     * @param on 是否启用：1是，0否
     * @param text 标题（可选）
     * @param contentId 跳转ID（可选）
     * @return 是否更新成功
     */
    boolean update(Integer bannerId, String image, Integer type, Integer on, String text, Integer contentId);

    /**
     * 删除Banner
     * @param bannerId Banner ID
     * @return 是否删除成功
     */
    boolean removeById(Integer bannerId);

    /**
     * 更新Banner排序
     * @param sortList 排序列表，包含bannerId和sort
     * @return 是否更新成功
     */
    boolean updateSort(List<Map<String, Integer>> sortList);
}
