package com.example.wx.mapper;

import com.example.common.pojo.Banner;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-20
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    @Select("SELECT banner_id, image, type, `on`, text, content_id, sort FROM banner ORDER BY sort ASC")
    List<Banner> selectAllList();
    
    @Select("SELECT banner_id, image, type, `on`, text, content_id, sort FROM banner WHERE `on` = 1 ORDER BY sort ASC")
    List<Banner> selectEnabledList();
}
