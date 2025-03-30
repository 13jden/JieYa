package com.example.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.WxDto.VenueBookingDto;
import com.example.common.pojo.VenueBooking;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
public interface VenueBookingService extends IService<VenueBooking> {
    // 分页查询预约记录
    Page<VenueBookingDto> pageBookings(Integer current, Integer size, String userId,
                                       Integer venueId, String status, Date startDate, Date endDate);

    // 获取预约详情
    VenueBookingDto getBookingDetailById(String id);

    // 根据用户ID查询预约记录
    List<VenueBookingDto> getBookingsByUserId(String userId);

    // 根据场地ID查询预约记录
    List<VenueBookingDto> getBookingsByVenueId(Integer venueId);

    // 根据日期查询预约记录
    List<VenueBookingDto> getBookingsByDate(Date date);

    // 根据场地ID和日期查询预约记录
    List<VenueBookingDto> getBookingsByVenueAndDate(Integer venueId, Date date);

    // 更新预约状态
    boolean updateBookingStatus(String id, String status);
}
