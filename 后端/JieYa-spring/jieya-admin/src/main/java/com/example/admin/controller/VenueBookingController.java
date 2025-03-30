package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.service.VenueBookingService;
import com.example.common.WxDto.VenueBookingDto;
import com.example.common.common.Result;
import com.example.common.pojo.VenueBooking;
import com.example.common.redis.RedisComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  场地预约前端控制器
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@RestController
@RequestMapping("/venue-booking")
public class VenueBookingController {
    
    @Autowired
    private VenueBookingService venueBookingService;
    
    @Autowired
    private RedisComponent redisComponent;
    
    /**
     * 分页查询所有预约记录
     */
    @GetMapping("/page")
    public Result page(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "venueId", required = false) Integer venueId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        try {
            Page<VenueBookingDto> page = venueBookingService.pageBookings(
                    current, size, userId, venueId, status, startDate, endDate);
            return Result.success(page);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询预约记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询预约详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable String id) {
        try {
            VenueBookingDto bookingDto = venueBookingService.getBookingDetailById(id);
            if (bookingDto == null) {
                return Result.error("预约记录不存在");
            }
            return Result.success(bookingDto);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询预约详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID查询预约记录
     */
    @GetMapping("/user/{userId}")
    public Result getByUserId(@PathVariable String userId) {
        try {
            List<VenueBookingDto> bookings = venueBookingService.getBookingsByUserId(userId);
            return Result.success(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询用户预约记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据场地ID查询预约记录
     */
    @GetMapping("/venue/{venueId}")
    public Result getByVenueId(@PathVariable Integer venueId) {
        try {
            List<VenueBookingDto> bookings = venueBookingService.getBookingsByVenueId(venueId);
            return Result.success(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询场地预约记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据日期查询预约记录
     */
    @GetMapping("/date")
    public Result getByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        try {
            List<VenueBookingDto> bookings = venueBookingService.getBookingsByDate(date);
            return Result.success(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询日期预约记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新预约状态
     */
    @PostMapping("/update-status")
    public Result updateStatus(
            @RequestParam String id, 
            @RequestParam String status) {
        
        try {
            boolean success = venueBookingService.updateBookingStatus(id, status);
            if (success) {
                // 更新成功后，删除用户预约历史缓存
                VenueBooking booking = venueBookingService.getById(id);
                if (booking != null) {
                    redisComponent.deleteUserBookingHistory(booking.getUserId());
                }
                return Result.success("更新状态成功");
            } else {
                return Result.error("更新状态失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新预约状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除预约记录
     */
    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable String id) {
        try {
            // 先获取用户ID，以便删除缓存
            VenueBooking booking = venueBookingService.getById(id);
            if (booking == null) {
                return Result.error("预约记录不存在");
            }
            
            boolean success = venueBookingService.removeById(id);
            if (success) {
                // 删除成功后，删除用户预约历史缓存
                redisComponent.deleteUserBookingHistory(booking.getUserId());
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除预约记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除预约记录
     */
    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<String> ids) {
        try {
            // 获取所有相关用户ID
            QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", ids);
            List<VenueBooking> bookings = venueBookingService.list(queryWrapper);
            
            boolean success = venueBookingService.removeByIds(ids);
            if (success) {
                // 删除成功后，删除所有相关用户的预约历史缓存
                for (VenueBooking booking : bookings) {
                    redisComponent.deleteUserBookingHistory(booking.getUserId());
                }
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除预约记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询某场地某天的所有预约时段
     */
    @GetMapping("/venue-date")
    public Result getByVenueAndDate(
            @RequestParam Integer venueId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        try {
            List<VenueBookingDto> bookings = venueBookingService.getBookingsByVenueAndDate(venueId, date);
            return Result.success(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询场地日期预约记录失败：" + e.getMessage());
        }
    }
}

