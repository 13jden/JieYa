package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.WxDto.TokenUserInfoDto;
import com.example.common.adminDto.VenueDto;
import com.example.common.common.Result;
import com.example.common.constants.Constants;
import com.example.common.pojo.Venue;
import com.example.common.pojo.VenueBooking;
import com.example.common.pojo.VenueImage;
import com.example.common.redis.RedisComponent;
import com.example.common.utils.CopyTools;
import com.example.common.utils.StringTools;
import com.example.wx.dto.TimeSlotDto;
import com.example.wx.mapper.VenueBookingMapper;
import com.example.wx.mapper.VenueImageMapper;
import com.example.wx.mapper.VenueMapper;
import com.example.wx.service.VenueBookingService;
import com.example.wx.service.VenueService;
import com.example.common.WxDto.VenueBookingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-03-23
 */
@Service
public class VenueBookingServiceImpl extends ServiceImpl<VenueBookingMapper, VenueBooking> implements VenueBookingService {

    @Autowired
    private VenueMapper venueMapper;
    
    @Autowired
    private VenueService venueService;
    
    @Autowired
    private VenueImageMapper venueImageMapper;

    @Autowired
    private RedisComponent redisComponent;

    @Value("${host.url}")
    private String hostUrl;
    
    @Value("${venue.booking.start.time}")
    private String bookingStartTime;
    
    @Value("${venue.booking.end.time}")
    private String bookingEndTime;

    @Override
    public List<TimeSlotDto> getAvailableTimeSlots(Integer venueId, Date date) {
        // 1. 检查场地是否存在
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new RuntimeException("场地不存在");
        }
        
        // 2. 获取当天的所有预约记录
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date startOfNextDay = calendar.getTime();
        
        QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("venue_id", venueId)
                  .ge("start_time", startOfDay)
                  .lt("start_time", startOfNextDay)
                  .eq("status", "已确认");
        
        List<VenueBooking> bookings = this.list(queryWrapper);
        
        // 3. 生成时间段列表
        List<TimeSlotDto> timeSlots = generateTimeSlots(date);
        
        // 4. 标记已预约的时间段
        markBookedTimeSlots(timeSlots, bookings, date);
        
        return timeSlots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createBooking(Integer venueId, String userId, Date startTime, Date endTime) {
        // 1. 检查场地是否存在
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new RuntimeException("场地不存在");
        }
        
        // 2. 验证时间格式（确保是整点或半点）
        validateTimeFormat(startTime, endTime);
        
        // 3. 检查开始时间和结束时间是否合法
        if (startTime.after(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("预约开始时间必须早于结束时间");
        }
        
        // 4. 检查是否在营业时间内
        validateBusinessHours(startTime, endTime);
        
        // 5. 检查时间段是否可用
        checkTimeSlotAvailability(venueId, startTime, endTime);
        
        // 6. 计算总价
        BigDecimal totalPrice = calculateTotalPrice(venue, startTime, endTime);
        
        // 7. 创建预约记录
        VenueBooking booking = new VenueBooking();
        booking.setId(StringTools.getRandomBumber(Constants.LENGTH_10));
        booking.setVenueId(venueId);
        booking.setUserId(userId);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setTotalPrice(totalPrice);
        booking.setStatus("已确认");  // 直接设置为已确认状态，可根据业务需求修改
        booking.setCreatedAt(new Date());
        booking.setUpdatedAt(new Date());
        
        // 8. 保存预约记录
        this.save(booking);
        
        // 创建成功后，删除缓存
        redisComponent.deleteUserBookingHistory(userId);
        
        return booking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VenueBookingDto cancelBooking(String bookingId, String userId) {
        // 1. 检查预约记录是否存在
        VenueBooking booking = this.getById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 2. 验证用户身份
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约记录");
        }
        
        // 3. 检查预约状态
        if (!"已确认".equals(booking.getStatus())) {
            throw new RuntimeException("当前预约状态无法取消");
        }
        
        // 4. 检查预约时间
        Date now = new Date();
        if (now.after(booking.getStartTime())) {
            throw new RuntimeException("预约已开始，无法取消");
        }
        
        // 5. 更新预约状态
        booking.setStatus("已取消");
        booking.setUpdatedAt(new Date());
        this.updateById(booking);
        VenueDto venueDto = redisComponent.getVenue(booking.getVenueId());
        String coverImageUrl = venueDto.getCoverImage();
        Venue venue = CopyTools.copy(venueDto,Venue.class);
        if(venueDto==null){
            // 6. 获取场馆信息
            venue = venueMapper.selectById(booking.getVenueId());

            // 7. 获取场馆封面图
            VenueImage coverImage = venueImageMapper.findByVenueIdAndSortOrder(booking.getVenueId(), 1);
             coverImageUrl= coverImage != null ? hostUrl+"/images/venue/"+coverImage.getImageUrl(): "";
        }

        // 取消成功后，删除缓存
        redisComponent.deleteUserBookingHistory(userId);
        
        return VenueBookingDto.fromVenueBooking(booking, venue, coverImageUrl);
    }

    @Override
    public List<VenueBookingDto> getBookingHistoryByUserId(String userId) {
        // 尝试从Redis获取
        List<VenueBookingDto> bookingHistory = redisComponent.getUserBookingHistory(userId);
        if (bookingHistory != null) {
            return bookingHistory;
        }
        
        // Redis中不存在，从数据库获取
        QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                  .orderByDesc("created_at");
        
        List<VenueBooking> bookings = this.list(queryWrapper);
        
        // 如果没有预约记录，直接返回空列表
        if (bookings.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 获取所有相关场地的信息
        Set<Integer> venueIds = bookings.stream()
                .map(VenueBooking::getVenueId)
                .collect(Collectors.toSet());
        
        Map<Integer, Venue> venueMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<Venue> venues = venueMapper.selectBatchIds(venueIds);
            venueMap = venues.stream()
                    .collect(Collectors.toMap(Venue::getId, venue -> venue));
        }

        Map<Integer, String> venueCoverMap = new HashMap<>();
        for (Integer venueId : venueIds) {
            // 查询排序为1的图片（封面图）
            VenueImage coverImage = venueImageMapper.findByVenueIdAndSortOrder(venueId, 1);
            if (coverImage != null) {
                venueCoverMap.put(venueId, hostUrl+"/images/venue/"+coverImage.getImageUrl());
            }
        }
        
        // 4. 组装返回数据
        bookingHistory = new ArrayList<>();
        
        for (VenueBooking booking : bookings) {
            Venue venue = venueMap.get(booking.getVenueId());
            String coverImageUrl = venueCoverMap.getOrDefault(booking.getVenueId(), "");
            VenueBookingDto dto = VenueBookingDto.fromVenueBooking(booking, venue, coverImageUrl);
            bookingHistory.add(dto);
        }
        
        // 存入Redis缓存
        redisComponent.setUserBookingHistory(userId, bookingHistory);
        
        return bookingHistory;
    }

    @Override
    public VenueBookingDto getBookingDetail(String bookingId, String userId) {
        // 1. 获取预约记录
        VenueBooking booking = this.getById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 2. 验证用户身份
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此预约记录");
        }
        VenueDto venueDto = redisComponent.getVenue(booking.getVenueId());
        if (venueDto==null){
           venueDto = venueService.getVenueWithImagesById(booking.getVenueId());
        }

        if (venueDto == null) {
            throw new RuntimeException("场地信息不存在");
        }
        
        // 4. 构建并返回详细DTO
        return VenueBookingDto.fromVenueBookingWithDetail(booking, venueDto,hostUrl);
    }
    
    /**
     * 生成一天的时间段列表
     */
    private List<TimeSlotDto> generateTimeSlots(Date date) {
        List<TimeSlotDto> timeSlots = new ArrayList<>();
        
        try {
            // 解析营业时间
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Date startTime = timeFormat.parse(bookingStartTime);
            Date endTime = timeFormat.parse(bookingEndTime);
            
            // 设置日期部分
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            
            Calendar slotStart = Calendar.getInstance();
            slotStart.setTime(startTime);
            slotStart.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            slotStart.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            slotStart.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
            
            Calendar slotEnd = Calendar.getInstance();
            slotEnd.setTime(slotStart.getTime());
            slotEnd.add(Calendar.MINUTE, 30);  // 每个时间段30分钟
            
            Calendar businessEnd = Calendar.getInstance();
            businessEnd.setTime(endTime);
            businessEnd.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
            businessEnd.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            businessEnd.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
            
            // 生成所有时间段
            while (slotEnd.getTime().compareTo(businessEnd.getTime()) <= 0) {
                TimeSlotDto slot = new TimeSlotDto(
                        timeFormat.format(slotStart.getTime()),
                        timeFormat.format(slotEnd.getTime()),
                        true  // 默认可用
                );
                timeSlots.add(slot);
                
                // 移动到下一个时间段
                slotStart.add(Calendar.MINUTE, 30);
                slotEnd.add(Calendar.MINUTE, 30);
            }
        } catch (Exception e) {
            throw new RuntimeException("生成时间段失败", e);
        }
        
        return timeSlots;
    }
    
    /**
     * 标记已预约的时间段
     */
    private void markBookedTimeSlots(List<TimeSlotDto> timeSlots, List<VenueBooking> bookings, Date date) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        // 遍历所有预约记录
        for (VenueBooking booking : bookings) {
            String bookingStartTimeStr = timeFormat.format(booking.getStartTime());
            String bookingEndTimeStr = timeFormat.format(booking.getEndTime());
            
            // 标记被预约的时间段为不可用
            for (TimeSlotDto slot : timeSlots) {
                // 检查时间段是否与预约重叠
                if ((slot.getStartTime().compareTo(bookingStartTimeStr) >= 0 && 
                     slot.getStartTime().compareTo(bookingEndTimeStr) < 0) ||
                    (slot.getEndTime().compareTo(bookingStartTimeStr) > 0 && 
                     slot.getEndTime().compareTo(bookingEndTimeStr) <= 0) ||
                    (slot.getStartTime().compareTo(bookingStartTimeStr) <= 0 && 
                     slot.getEndTime().compareTo(bookingEndTimeStr) >= 0)) {
                    slot.setAvailable(false);
                }
            }
        }
        
        // 过去的时间段标记为不可用
        Date now = new Date();
        String currentTimeStr = timeFormat.format(now);
        
        // 检查是否是当天
        Calendar today = Calendar.getInstance();
        Calendar slotDate = Calendar.getInstance();
        slotDate.setTime(date);
        
        boolean isToday = today.get(Calendar.YEAR) == slotDate.get(Calendar.YEAR) &&
                          today.get(Calendar.DAY_OF_MONTH) == slotDate.get(Calendar.DAY_OF_MONTH) &&
                          today.get(Calendar.MONTH) == slotDate.get(Calendar.MONTH);
        
        // 如果是当天，则标记已过去的时间段为不可用
        if (isToday) {
            for (TimeSlotDto slot : timeSlots) {
                if (slot.getEndTime().compareTo(currentTimeStr) <= 0) {
                    slot.setAvailable(false);
                }
            }
        }
    }
    
    /**
     * 验证时间格式（确保是整点或半点）
     */
    private void validateTimeFormat(Date startTime, Date endTime) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startTime);
        int startMinute = startCal.get(Calendar.MINUTE);
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endTime);
        int endMinute = endCal.get(Calendar.MINUTE);
        
        if ((startMinute != 0 && startMinute != 30) || (endMinute != 0 && endMinute != 30)) {
            throw new RuntimeException("预约时间必须是整点或半点（如10:00或10:30）");
        }
    }
    
    /**
     * 验证是否在营业时间内
     */
    private void validateBusinessHours(Date startTime, Date endTime) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startTime);
            String startTimeStr = String.format("%02d:%02d", startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE));
            
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endTime);
            String endTimeStr = String.format("%02d:%02d", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE));
            
            Date businessStart = timeFormat.parse(bookingStartTime);
            Date businessEnd = timeFormat.parse(bookingEndTime);
            Date bookingStart = timeFormat.parse(startTimeStr);
            Date bookingEnd = timeFormat.parse(endTimeStr);
            
            if (bookingStart.before(businessStart) || bookingEnd.after(businessEnd)) {
                throw new RuntimeException("预约时间必须在营业时间内（" + bookingStartTime + "-" + bookingEndTime + "）");
            }
        } catch (Exception e) {
            throw new RuntimeException("验证营业时间失败", e);
        }
    }
    
    /**
     * 检查时间段是否可用
     */
    private void checkTimeSlotAvailability(Integer venueId, Date startTime, Date endTime) {
        // 查询与指定时间段重叠的预约记录
        QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("venue_id", venueId)
                  .eq("status", "已确认")
                  .and(wrapper -> wrapper
                        .and(w -> w
                            .lt("start_time", endTime)
                            .gt("start_time", startTime))
                        .or(w -> w
                            .lt("end_time", endTime)
                            .gt("end_time", startTime))
                        .or(w -> w
                            .le("start_time", startTime)
                            .ge("end_time", endTime)));
        
        int count = (int) this.count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("所选时间段已被预约，请选择其他时间");
        }
    }
    
    /**
     * 计算预约总价
     */
    private BigDecimal calculateTotalPrice(Venue venue, Date startTime, Date endTime) {
        // 计算预约时长（小时）
        long duration = endTime.getTime() - startTime.getTime();
        double hours = (double) duration / (1000 * 60 * 60);
        
        // 计算总价（场地单价 * 时长）
        return venue.getPrice().multiply(new BigDecimal(hours));
    }
    
    /**
     * 解析时间字符串
     */
    private Date parseTime(String timeStr) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            return timeFormat.parse(timeStr);
        } catch (Exception e) {
            throw new RuntimeException("解析时间失败", e);
        }
    }
}
