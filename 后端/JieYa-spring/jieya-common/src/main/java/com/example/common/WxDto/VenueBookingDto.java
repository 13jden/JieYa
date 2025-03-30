package com.example.common.WxDto;

import com.example.common.adminDto.VenueDto;
import com.example.common.pojo.Venue;
import com.example.common.pojo.VenueBooking;
import com.example.common.pojo.VenueImage;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 场馆预约DTO
 * 包含预约记录和场馆基本信息
 */
@Data
public class VenueBookingDto {

    // 预约记录ID
    private String id;
    
    // 场馆ID
    private Integer venueId;
    
    // 场馆名称
    private String venueName;
    
    // 场馆位置
    private String venueLocation;
    
    // 场馆描述
    private String venueContent;
    
    // 场馆封面图
    private String venueCoverImage;
    
    // 用户ID
    private String userId;
    
    // 预约开始时间
    private Date startTime;
    
    // 格式化的开始时间
    private String startTimeStr;
    
    // 预约结束时间
    private Date endTime;
    
    // 格式化的结束时间
    private String endTimeStr;
    
    // 预约总价
    private BigDecimal totalPrice;
    
    // 预约状态
    private String status;
    
    // 创建时间
    private Date createdAt;
    
    // 格式化的创建时间
    private String createdAtStr;
    
    // 更新时间
    private Date updatedAt;
    
    // 完整的场馆信息（用于详情页面）
    private VenueDto venueDetail;
    
    // 从VenueBooking和Venue构建基本DTO
    public static VenueBookingDto fromVenueBooking(VenueBooking booking, 
                                                 Venue venue,
                                                 String venueCoverImage) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        VenueBookingDto dto = new VenueBookingDto();
        dto.setId(booking.getId());
        dto.setVenueId(booking.getVenueId());
        
        if (venue != null) {
            dto.setVenueName(venue.getName());
            dto.setVenueLocation(venue.getLocation());
            dto.setVenueContent(venue.getContent());
        } else {
            dto.setVenueName("未知场馆");
        }
        
        dto.setVenueCoverImage(venueCoverImage);
        dto.setUserId(booking.getUserId());
        
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        
        // 设置格式化的时间字符串
        if (booking.getStartTime() != null) {
            dto.setStartTimeStr(sdf.format(booking.getStartTime()));
        }
        if (booking.getEndTime() != null) {
            dto.setEndTimeStr(sdf.format(booking.getEndTime()));
        }
        if (booking.getCreatedAt() != null) {
            dto.setCreatedAtStr(sdf.format(booking.getCreatedAt()));
        }
        
        return dto;
    }
    
    // 从VenueBooking和VenueDto构建详细DTO（包含完整场馆信息）
    public static VenueBookingDto fromVenueBookingWithDetail(VenueBooking booking, VenueDto venueDto,String hostUrl) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        VenueBookingDto dto = new VenueBookingDto();
        dto.setId(booking.getId());
        dto.setVenueId(booking.getVenueId());
        
        if (venueDto != null) {
            dto.setVenueName(venueDto.getName());
            dto.setVenueLocation(venueDto.getLocation());
            dto.setVenueContent(venueDto.getContent());
            if (venueDto.getCoverImage() != null) {
                dto.setVenueCoverImage(hostUrl + "/images/venue/"+ venueDto.getCoverImage());
            }
            dto.setVenueDetail(venueDto);
        } else {
            dto.setVenueName("未知场馆");
        }
        
        dto.setUserId(booking.getUserId());
        
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        
        // 设置格式化的时间字符串
        if (booking.getStartTime() != null) {
            dto.setStartTimeStr(sdf.format(booking.getStartTime()));
        }
        if (booking.getEndTime() != null) {
            dto.setEndTimeStr(sdf.format(booking.getEndTime()));
        }
        if (booking.getCreatedAt() != null) {
            dto.setCreatedAtStr(sdf.format(booking.getCreatedAt()));
        }
        
        return dto;
    }
}