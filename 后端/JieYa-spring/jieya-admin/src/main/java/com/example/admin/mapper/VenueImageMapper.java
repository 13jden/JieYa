package com.example.admin.mapper;

import com.example.common.pojo.VenueImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Mapper
public interface VenueImageMapper extends BaseMapper<VenueImage> {

    /**
     * 根据场馆ID和排序值查询图片
     */
    @Select("SELECT * FROM venue_image WHERE venue_id = #{venueId} AND sort_order = #{sortOrder}")
    VenueImage findByVenueIdAndSortOrder(@Param("venueId") Integer venueId, @Param("sortOrder") Integer sortOrder);
}
