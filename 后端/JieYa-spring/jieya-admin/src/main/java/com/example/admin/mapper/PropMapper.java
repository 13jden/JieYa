package com.example.admin.mapper;

import com.example.common.pojo.Prop;
import com.example.common.pojo.PropImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p>
 *  道具 Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Mapper
public interface PropMapper extends BaseMapper<Prop> {

    /**
     * 查询道具的封面图片（排序为1的图片）
     */
    @Select("SELECT * FROM prop_image WHERE prop_id = #{propId} AND sort_order = 1 LIMIT 1")
    PropImage findCoverImageByPropId(@Param("propId") Integer propId);
    
    /**
     * 根据道具ID查询所有图片，按排序升序
     */
    @Select("SELECT * FROM prop_image WHERE prop_id = #{propId} ORDER BY sort_order ASC")
    List<PropImage> findAllImagesByPropId(@Param("propId") Integer propId);
    
    /**
     * 查询最近租借过的道具ID列表
     */
    @Select("SELECT DISTINCT prop_id FROM prop_rental WHERE created_at > DATE_SUB(NOW(), INTERVAL 30 DAY) ORDER BY created_at DESC LIMIT #{limit}")
    List<Integer> findRecentlyRentedPropIds(@Param("limit") Integer limit);
}
