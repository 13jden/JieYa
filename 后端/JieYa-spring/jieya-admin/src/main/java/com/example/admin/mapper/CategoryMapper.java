package com.example.admin.mapper;

import com.example.common.pojo.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 获取最大排序值
     * @return 最大排序值
     */
    @Select("SELECT MAX(sort) FROM category")
    Integer getMaxSort();
    
    /**
     * 更新分类排序
     * @param id 分类ID
     * @param sort 排序值
     * @return 影响行数
     */
    @Update("UPDATE category SET sort = #{sort} WHERE category_id = #{id}")
    int updateSort(@Param("id") Integer id, @Param("sort") Integer sort);
    
    /**
     * 删除分类后更新其他分类的排序值
     * @param deletedSort 被删除的分类的排序值
     * @return 是否更新成功
     */
    @Update("UPDATE category SET sort = sort - 1 WHERE sort > #{deletedSort}")
    boolean updateSortAfterDelete(@Param("deletedSort") Integer deletedSort);
}
