package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.admin.mapper.UserMapper;
import com.example.admin.mapper.VenueBookingMapper;
import com.example.admin.mapper.VenueImageMapper;
import com.example.admin.mapper.VenueMapper;
import com.example.admin.service.VenueBookingService;
import com.example.common.WxDto.VenueBookingDto;
import com.example.common.pojo.Venue;
import com.example.common.pojo.VenueBooking;
import com.example.common.pojo.VenueImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private VenueImageMapper venueImageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private VenueBookingMapper venueBookingMapper;
    
    @Value("${host.url}")
    private String hostUrl;
    
    @Override
    public Page<VenueBookingDto> pageBookings(Integer current, Integer size, String userId, 
                                             Integer venueId, String status, Date startDate, Date endDate) {
        // 1. 构建查询条件
        QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(userId)) {
            queryWrapper.eq("user_id", userId);
        }
        if (venueId != null) {
            queryWrapper.eq("venue_id", venueId);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (startDate != null) {
            queryWrapper.ge("start_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("start_time", endDate);
        }
        queryWrapper.orderByDesc("created_at");
        
        // 2. 分页查询
        Page<VenueBooking> page = new Page<>(current, size);
        page = this.page(page, queryWrapper);
        
        // 3. 查询相关场地信息
        List<VenueBooking> bookings = page.getRecords();
        Page<VenueBookingDto> dtoPage = new Page<>(
                page.getCurrent(), page.getSize(), page.getTotal());
        
        if (bookings.isEmpty()) {
            dtoPage.setRecords(new ArrayList<>());
            return dtoPage;
        }
        
        // 4. 获取相关场地ID
        Set<Integer> venueIds = bookings.stream()
                .map(VenueBooking::getVenueId)
                .collect(Collectors.toSet());
        
        // 5. 批量查询场地信息
        Map<Integer, Venue> venueMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<Venue> venues = venueMapper.selectBatchIds(venueIds);
            venueMap = venues.stream()
                    .collect(Collectors.toMap(Venue::getId, venue -> venue));
        }
        
        // 6. 查询场地封面图
        Map<Integer, String> venueCoverMap = getVenueCoverImages(venueIds);
        
        // 7. 转换为DTO对象
        Map<Integer, Venue> finalVenueMap = venueMap;
        List<VenueBookingDto> dtoList = bookings.stream()
                .map(booking -> {
                    Venue venue = finalVenueMap.getOrDefault(booking.getVenueId(), new Venue());
                    String coverImage = venueCoverMap.getOrDefault(booking.getVenueId(), "");
                    
                    return VenueBookingDto.fromVenueBooking(
                            booking,
                            venue,
                            coverImage
                    );
                })
                .collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public VenueBookingDto getBookingDetailById(String id) {
        // 1. 查询预约记录
        VenueBooking booking = this.getById(id);
        if (booking == null) {
            return null;
        }
        
        // 2. 查询场地信息
        Venue venue = venueMapper.selectById(booking.getVenueId());
        if (venue == null) {
            venue = new Venue();
        }
        
        // 3. 查询场地封面图
        Map<Integer, String> venueCoverMap = getVenueCoverImages(Collections.singleton(booking.getVenueId()));
        String coverImage = venueCoverMap.getOrDefault(booking.getVenueId(), "");
        
        // 4. 转换为DTO并返回
        return VenueBookingDto.fromVenueBooking(
                booking,
                venue,
                coverImage
        );
    }

    @Override
    public List<VenueBookingDto> getBookingsByUserId(String userId) {
        // 1. 查询用户的所有预约记录
        QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                  .orderByDesc("created_at");
        
        List<VenueBooking> bookings = this.list(queryWrapper);
        if (bookings.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 获取相关场地ID
        Set<Integer> venueIds = bookings.stream()
                .map(VenueBooking::getVenueId)
                .collect(Collectors.toSet());
        
        // 3. 批量查询场地信息
        Map<Integer, Venue> venueMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<Venue> venues = venueMapper.selectBatchIds(venueIds);
            venueMap = venues.stream()
                    .collect(Collectors.toMap(Venue::getId, venue -> venue));
        }
        
        // 4. 查询场地封面图
        Map<Integer, String> venueCoverMap = getVenueCoverImages(venueIds);
        
        // 5. 转换为DTO对象
        Map<Integer, Venue> finalVenueMap = venueMap;
        return bookings.stream()
                .map(booking -> {
                    Venue venue = finalVenueMap.getOrDefault(booking.getVenueId(), new Venue());
                    String coverImage = venueCoverMap.getOrDefault(booking.getVenueId(), "");
                    
                    return VenueBookingDto.fromVenueBooking(
                            booking,
                            venue,
                            coverImage
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<VenueBookingDto> getBookingsByVenueId(Integer venueId) {
        // 1. 查询场地的所有预约记录
        QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("venue_id", venueId)
                  .orderByDesc("created_at");
        
        List<VenueBooking> bookings = this.list(queryWrapper);
        if (bookings.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 查询场地信息
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            venue = new Venue();
        }
        
        // 3. 查询场地封面图
        Map<Integer, String> venueCoverMap = getVenueCoverImages(Collections.singleton(venueId));
        String coverImage = venueCoverMap.getOrDefault(venueId, "");
        
        // 4. 转换为DTO对象
        Venue finalVenue = venue;
        return bookings.stream()
                .map(booking -> VenueBookingDto.fromVenueBooking(
                        booking,
                        finalVenue,
                        coverImage
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<VenueBookingDto> getBookingsByDate(Date date) {
        // 设置查询的日期范围
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endDate = calendar.getTime();
        
        // 1. 查询该日期的所有预约记录
        QueryWrapper<VenueBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("start_time", startDate)
                  .lt("start_time", endDate)
                  .orderByAsc("start_time");
        
        List<VenueBooking> bookings = this.list(queryWrapper);
        if (bookings.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 获取相关场地ID
        Set<Integer> venueIds = bookings.stream()
                .map(VenueBooking::getVenueId)
                .collect(Collectors.toSet());
        
        // 3. 批量查询场地信息
        Map<Integer, Venue> venueMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<Venue> venues = venueMapper.selectBatchIds(venueIds);
            venueMap = venues.stream()
                    .collect(Collectors.toMap(Venue::getId, venue -> venue));
        }
        
        // 4. 查询场地封面图
        Map<Integer, String> venueCoverMap = getVenueCoverImages(venueIds);
        
        // 5. 转换为DTO对象
        Map<Integer, Venue> finalVenueMap = venueMap;
        return bookings.stream()
                .map(booking -> {
                    Venue venue = finalVenueMap.getOrDefault(booking.getVenueId(), new Venue());
                    String coverImage = venueCoverMap.getOrDefault(booking.getVenueId(), "");
                    
                    return VenueBookingDto.fromVenueBooking(
                            booking,
                            venue,
                            coverImage
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<VenueBookingDto> getBookingsByVenueAndDate(Integer venueId, Date date) {
        // 设置日期的开始和结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endDate = calendar.getTime();
        
        // 使用新的Mapper方法查询预约
        List<VenueBooking> bookings = venueBookingMapper.findBookingsByVenueAndDay(venueId, startDate, endDate);
        if (bookings.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询场地信息
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null) {
            venue = new Venue();
        }
        
        // 查询场地封面图
        VenueImage coverImage = venueImageMapper.findByVenueIdAndSortOrder(venueId, 1);
        String imageUrl = "";
        if (coverImage != null) {
            imageUrl = hostUrl + "/images/venue/" + coverImage.getImageUrl();
        }
        
        // 转换为DTO对象
        Venue finalVenue = venue;
        String finalImageUrl = imageUrl;
        return bookings.stream()
            .map(booking -> VenueBookingDto.fromVenueBooking(
                booking,
                finalVenue,
                    finalImageUrl
            ))
            .collect(Collectors.toList());
    }

    @Override
    public boolean updateBookingStatus(String id, String status) {
        if (!StringUtils.hasText(id) || !StringUtils.hasText(status)) {
            return false;
        }
        
        UpdateWrapper<VenueBooking> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                   .set("status", status)
                   .set("updated_at", new Date());
        
        return this.update(updateWrapper);
    }
    
    /**
     * 获取场地封面图
     */
    private Map<Integer, String> getVenueCoverImages(Set<Integer> venueIds) {
        Map<Integer, String> coverImages = new HashMap<>();
        
        for (Integer venueId : venueIds) {
            // 此处应该查询场地的封面图，实现可能需要根据实际情况调整
            // 示例实现：查询排序为1的图片作为封面
            VenueImage coverImage = venueImageMapper.findByVenueIdAndSortOrder(venueId, 1);
            if (coverImage != null) {
                coverImages.put(venueId, hostUrl + "/images/venue/" + coverImage.getImageUrl());
            }
        }
        
        return coverImages;
    }
}
