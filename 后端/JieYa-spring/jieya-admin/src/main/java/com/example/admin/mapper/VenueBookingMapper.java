package com.example.admin.mapper;

import com.example.common.pojo.VenueBooking;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  场地预约 Mapper 接口
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Mapper
public interface VenueBookingMapper extends BaseMapper<VenueBooking> {
    
    /**
     * 查询某个场地某天内的所有预约
     */
    @Select("SELECT * FROM venue_booking WHERE venue_id = #{venueId} " +
            "AND start_time >= #{dayStart} AND start_time < #{nextDayStart} " +
            "AND status != '已取消' AND status != '已拒绝' " +
            "ORDER BY start_time ASC")
    List<VenueBooking> findBookingsByVenueAndDay(
            @Param("venueId") Integer venueId, 
            @Param("dayStart") Date dayStart, 
            @Param("nextDayStart") Date nextDayStart);
    
    /**
     * 查询某个场地在指定时间段的预约（用于检查时间冲突）
     */
    @Select("SELECT * FROM venue_booking WHERE venue_id = #{venueId} " +
            "AND start_time < #{endTime} AND end_time > #{startTime} " +
            "AND status != '已取消' AND status != '已拒绝'")
    List<VenueBooking> findOverlappingBookings(
            @Param("venueId") Integer venueId, 
            @Param("startTime") Date startTime, 
            @Param("endTime") Date endTime);
    
    /**
     * 统计用户的预约次数
     */
    @Select("SELECT COUNT(*) FROM venue_booking WHERE user_id = #{userId}")
    Integer countBookingsByUserId(@Param("userId") String userId);
    
    /**
     * 查询即将到来的预约（未来7天内）
     */
    @Select("SELECT * FROM venue_booking WHERE start_time >= NOW() " +
            "AND start_time <= DATE_ADD(NOW(), INTERVAL 7 DAY) " +
            "AND status = '已确认' ORDER BY start_time ASC")
    List<VenueBooking> findUpcomingBookings();
    
    /**
     * 查询某个月的所有预约
     */
    @Select("SELECT * FROM venue_booking WHERE " +
            "YEAR(start_time) = #{year} AND MONTH(start_time) = #{month} " +
            "ORDER BY start_time ASC")
    List<VenueBooking> findBookingsByMonth(@Param("year") Integer year, @Param("month") Integer month);
}
