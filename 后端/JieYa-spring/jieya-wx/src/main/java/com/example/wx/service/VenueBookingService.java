package com.example.wx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.common.Result;
import com.example.common.pojo.VenueBooking;
import com.example.wx.dto.TimeSlotDto;
import com.example.common.WxDto.VenueBookingDto;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
public interface VenueBookingService extends IService<VenueBooking> {

    /**
     * 获取场地在指定日期的可用时间段
     * @param venueId 场地ID
     * @param date 日期
     * @return 时间段列表，包含每个时间段是否可用的信息
     */
    List<TimeSlotDto> getAvailableTimeSlots(Integer venueId, Date date);
    
    /**
     * 创建场地预约
     * @param venueId 场地ID
     * @param userId 用户ID
     * @param startTime 预约开始时间
     * @param endTime 预约结束时间
     * @return 预约ID
     */
    String  createBooking(Integer venueId, String userId, Date startTime, Date endTime);
    
    /**
     * 取消预约
     * @param bookingId 预约ID
     * @param userId 用户ID
     * @return 结果
     */
    VenueBookingDto cancelBooking(String bookingId, String userId);
    
    /**
     * 获取用户的预约历史
     * @param userId 用户ID
     * @return 预约历史列表
     */
    List<VenueBookingDto> getBookingHistoryByUserId(String userId);
    
    /**
     * 获取预约详情
     * @param bookingId 预约ID
     * @param userId 用户ID
     * @return 预约详情
     */
    VenueBookingDto getBookingDetail(String bookingId, String userId);
}
