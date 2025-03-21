package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.mapper.CategoryMapper;

import com.example.admin.service.CategoryService;
import com.example.common.exception.BusinessException;
import com.example.common.pojo.Category;
import com.example.common.redis.RedisComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 分类服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisComponent redisComponent;

    /**
     * 获取排序后的分类列表
     * @return 分类列表
     */
    @Override
    public List<Category> getList() {
        List<Category> categories = redisComponent.getCategoryList();
        if (categories != null) {
            return categories;
        }
        
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        categories = categoryMapper.selectList(queryWrapper);
        redisComponent.setCategoryList(categories);
        
        return categories;
    }

    /**
     * 添加分类
     * @param categoryName 分类名称
     * @param categoryCode 分类编码
     * @return 是否添加成功
     */
    @Override
    public boolean add(String categoryName, String categoryCode) {
        // 检查分类编码是否已存在
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_code", categoryCode);
        if (categoryMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("分类编码已存在");
        }
        
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setCategoryCode(categoryCode);
        
        // 获取最大排序值并加1
        Integer maxSort = categoryMapper.getMaxSort();
        category.setSort(maxSort == null ? 1 : maxSort + 1);
        
        boolean result = this.save(category);
        if (!result) {
            throw new BusinessException("添加分类失败");
        }
        
        // 删除缓存
        redisComponent.deleteCategoryList();
        return true;
    }

    /**
     * 更新分类
     * @param id 分类ID
     * @param categoryName 分类名称
     * @param categoryCode 分类编码
     * @return 是否更新成功
     */
    @Override
    public boolean update(int id, String categoryName, String categoryCode) {
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查分类编码是否已被其他分类使用
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_code", categoryCode).ne("category_id", id);
        if (categoryMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("分类编码已被其他分类使用");
        }
        category.setCategoryName(categoryName);
        category.setCategoryCode(categoryCode);
        
        boolean result = this.updateById(category);
        if (!result) {
            throw new BusinessException("更新分类失败");
        }
        
        // 删除缓存
        redisComponent.deleteCategoryList();
        return true;
    }

    /**
     * 更新分类排序
     * @param sortList 排序列表，包含id和sort
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSort(List<Map<String, Integer>> sortList) {
        if (sortList == null || sortList.isEmpty()) {
            throw new BusinessException("排序列表不能为空");
        }
        
        for (Map<String, Integer> sortItem : sortList) {
            Integer id = sortItem.get("id");
            Integer sort = sortItem.get("sort");
            
            if (id == null || sort == null) {
                throw new BusinessException("排序项缺少必要参数");
            }

            if (this.getById(id) == null) {
                throw new BusinessException("ID为" + id + "的分类不存在");
            }
            
            // 使用mapper直接更新排序值
            int result = categoryMapper.updateSort(id, sort);
            if (result <= 0) {
                throw new BusinessException("更新ID为" + id + "的分类排序失败");
            }
        }
        
        // 删除缓存
        redisComponent.deleteCategoryList();
        return true;
    }
    
    /**
     * 删除分类
     * @param id 分类ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Integer id) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        
        // 检查分类是否存在
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 获取被删除分类的排序值
        Integer deletedSort = category.getSort();
        
        // 获取最大排序值
        Integer maxSort = categoryMapper.getMaxSort();
        
        // 删除分类
        boolean result = super.removeById(id);
        if (!result) {
            throw new BusinessException("删除分类失败");
        }

        if (deletedSort < maxSort) {
            boolean updateResult = categoryMapper.updateSortAfterDelete(deletedSort);
            if (!updateResult) {
                throw new BusinessException("更新其他分类排序失败");
            }
        }
        
        // 删除缓存
        redisComponent.deleteCategoryList();
        return true;
    }

}
