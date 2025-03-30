package com.example.wx.controller;

import com.example.common.common.Result;
import com.example.common.pojo.Venue;
import com.example.common.utils.JwtUtil;
import com.example.wx.dto.TimeSlotDto;
import com.example.wx.service.VenueBookingService;
import com.example.wx.service.VenueService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import com.example.common.WxDto.VenueBookingDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Slf4j
@RestController
@RequestMapping("/venue-booking")
public class VenueBookingController {
    
    @Autowired
    private VenueBookingService venueBookingService;

    @Autowired
    private VenueService venueService;
    
    /**
     * 查询场地某日期的可用时间段
     */
    @GetMapping("/available-time")
    public Result getAvailableTimeSlots(
            @RequestParam(required = false) Integer venueId,
            @RequestParam(required = false) String date) {
        try {
            // 详细的参数校验

            if (StringUtils.isEmpty(date)) {
                return Result.error("预约日期不能为空");
            }
            if (venueId == null) {
                return Result.error("场地ID不能为空");
            }
            // 打印接收到的参数
            log.info("接收到请求参数: venueId={}, date={}", venueId, date);
            
            // 日期格式校验
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false); // 设置为严格模式
            Date bookingDate;
            try {
                bookingDate = sdf.parse(date);
                // 创建一个表示昨天的日期
                Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DATE, -1);
                yesterday.set(Calendar.HOUR_OF_DAY, 0);
                yesterday.set(Calendar.MINUTE, 0);
                yesterday.set(Calendar.SECOND, 0);
                yesterday.set(Calendar.MILLISECOND, 0);
                
                // 检查日期是否早于昨天
                if (bookingDate.before(yesterday.getTime())) {
                    return Result.error("预约日期不能早于昨天");
                }
            } catch (ParseException e) {
                log.error("日期解析错误: {}", e.getMessage());
                return Result.error("日期格式错误，请使用yyyy-MM-dd格式");
            }
            
            // 检查场地是否存在
            if (venueService.getById(venueId)==null) {
                return Result.error("场地不存在");
            }
            
            List<TimeSlotDto> timeSlots = venueBookingService.getAvailableTimeSlots(venueId, bookingDate);
            return Result.success(timeSlots);
            
        } catch (Exception e) {
            log.error("获取可用时间段失败", e);
            return Result.error("获取可用时间段失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建场地预约
     */
    @PostMapping("/create")
    public Result createBooking(
            @RequestHeader("Authorization") String token,
            @NotNull Integer venueId,
            @NotEmpty String startTime,
            @NotEmpty String endTime) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)) {
                return Result.error("预约时间不能为空");
            }
            
            // 打印接收到的参数
            log.info("接收到预约请求: venueId={}, startTime={}, endTime={}", venueId, startTime, endTime);
            
            // 日期格式校验
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setLenient(false); // 设置为严格模式
            
            Date parsedStartTime;
            Date parsedEndTime;
            
            try {
                parsedStartTime = sdf.parse(startTime);
                parsedEndTime = sdf.parse(endTime);
            } catch (ParseException e) {
                return Result.error("日期格式不正确，请使用yyyy-MM-dd HH:mm格式");
            }
            
            // 验证开始时间是否早于结束时间
            if (parsedStartTime.after(parsedEndTime) || parsedStartTime.equals(parsedEndTime)) {
                return Result.error("开始时间必须早于结束时间");
            }
            
            // 创建一个表示昨天的日期
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);
            
            // 检查预约日期是否早于昨天
            if (parsedStartTime.before(yesterday.getTime())) {
                return Result.error("预约日期不能早于昨天");
            }
            
            // 检查场地是否存在
            if (venueService.getById(venueId)==null) {
                return Result.error("场地不存在");
            }
            
            // 创建预约
            String bookingId = venueBookingService.createBooking(venueId, userId, parsedStartTime, parsedEndTime);
            
            return Result.success(bookingId);
        } catch (Exception e) {
            log.error("创建预约时发生错误", e);
            return Result.error("创建预约失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消预约
     */
    @PostMapping("/cancel/{id}")
    public Result cancelBooking(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") String bookingId) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            // 取消预约
            VenueBookingDto canceledBooking = venueBookingService.cancelBooking(bookingId, userId);
            return Result.success(canceledBooking);
        } catch (Exception e) {
            log.error("取消预约时发生错误", e);
            return Result.error("取消预约失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户的预约历史
     */
    @GetMapping("/history")
    public Result getBookingHistory(@RequestHeader("Authorization") String token) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            // 获取用户的预约历史
            List<VenueBookingDto> bookingHistory = venueBookingService.getBookingHistoryByUserId(userId);
            
            return Result.success(bookingHistory);
        } catch (Exception e) {
            log.error("获取预约历史时发生错误", e);
            return Result.error("获取预约历史失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取预约详情
     */
    @GetMapping("/detail/{id}")
    public Result getBookingDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") String bookingId) {
        try {
            // 从token获取用户ID
            String userId = JwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            if (userId == null) {
                return Result.error("无效的认证信息");
            }
            
            // 获取预约详情
            VenueBookingDto bookingDetail = venueBookingService.getBookingDetail(bookingId, userId);
            return Result.success(bookingDetail);
        } catch (Exception e) {
            log.error("获取预约详情时发生错误", e);
            return Result.error("获取预约详情失败：" + e.getMessage());
        }
    }
}

