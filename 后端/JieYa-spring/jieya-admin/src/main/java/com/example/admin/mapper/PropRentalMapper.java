package com.example.admin.mapper;

import com.example.common.pojo.PropRental;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  道具租借 Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Mapper
public interface PropRentalMapper extends BaseMapper<PropRental> {

    /**
     * 查询某个道具在指定日期范围内的租借情况
     */
    @Select("SELECT * FROM prop_rental WHERE prop_id = #{propId} " +
            "AND ((start_date <= #{endDate} AND end_date >= #{startDate}) OR " +
            "(start_date BETWEEN #{startDate} AND #{endDate})) " +
            "AND status != '已取消' AND status != '已拒绝'")
    List<PropRental> findOverlappingRentals(
            @Param("propId") Integer propId, 
            @Param("startDate") Date startDate, 
            @Param("endDate") Date endDate);
    
    /**
     * 统计某个用户的租借记录数
     */
    @Select("SELECT COUNT(*) FROM prop_rental WHERE user_id = #{userId}")
    Integer countRentalsByUserId(@Param("userId") String userId);
    
    /**
     * 查询某个道具的所有租借记录，按时间倒序
     */
    @Select("SELECT * FROM prop_rental WHERE prop_id = #{propId} ORDER BY created_at DESC")
    List<PropRental> findAllRentalsByPropId(@Param("propId") Integer propId);
    
    /**
     * 查询某种状态的租借记录
     */
    @Select("SELECT * FROM prop_rental WHERE status = #{status} ORDER BY created_at DESC LIMIT #{limit}")
    List<PropRental> findRentalsByStatus(@Param("status") String status, @Param("limit") Integer limit);
}
