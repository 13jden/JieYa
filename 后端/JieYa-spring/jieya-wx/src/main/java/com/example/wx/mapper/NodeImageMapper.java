package com.example.wx.mapper;

import com.example.common.pojo.NodeImage;
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
 * @since 2025-04-01
 */
@Mapper

public interface NodeImageMapper extends BaseMapper<NodeImage> {

    @Select("SELECT * FROM node_image WHERE node_id = #{node_id}")
    List<NodeImage> selectByNodeId(String node_id);

    @Select("SELECT * FROM node_image WHERE node_id = #{node_id} ORDER BY sort ASC")
    List<NodeImage> selectByNodeIdOrderBySort(String node_id);

    
}
