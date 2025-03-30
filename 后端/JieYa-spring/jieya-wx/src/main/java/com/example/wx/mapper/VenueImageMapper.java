package com.example.wx.mapper;

import com.example.common.pojo.VenueImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

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


    @Select("SELECT * FROM venue_image WHERE venue_id = #{venueId} AND sort_order = #{sortOrder} LIMIT 1")
    VenueImage findByVenueIdAndSortOrder(@Param("venueId") Integer venueId, @Param("sortOrder") Integer sortOrder);
}
