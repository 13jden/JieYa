package com.example.admin.service;

import com.example.common.pojo.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
public interface CategoryService extends IService<Category> {

    boolean update(int id, String categoryName, String categoryCode);

    boolean add(String categoryName, String categoryCode);

    boolean updateSort(List<Map<String, Integer>> sortList);

    List<Category> getList();


    @Transactional(rollbackFor = Exception.class)
    boolean removeById(Integer id);
}
