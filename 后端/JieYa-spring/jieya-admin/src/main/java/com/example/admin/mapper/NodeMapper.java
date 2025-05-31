package com.example.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.common.pojo.Node;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 正式笔记表 Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Mapper
public interface NodeMapper extends BaseMapper<Node> {

    /**
     * 通过ID查询已审核的笔记
     */
    @Select("SELECT * FROM node WHERE id = #{id}")
    Node selectNodePostById(@Param("id") String id);
}
