package com.example.common.WxDto;

import com.example.common.pojo.PropRental;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 道具租借历史DTO
 * 包含租借记录和道具基本信息
 */
@Data
public class PropRentalDto {
    // 租借记录ID
    private String id;
    
    // 道具ID
    private Integer propId;
    
    // 道具名称
    private String propName;
    
    // 道具描述
    private String propDescription;
    
    // 道具价格
    private BigDecimal propPrice;
    
    // 道具封面图
    private String propCoverImage;
    
    // 用户ID
    private String userId;
    
    // 租借开始时间
    private Date startDate;
    
    // 租借结束时间
    private Date endDate;
    
    // 租借总价
    private BigDecimal totalPrice;
    
    // 租借状态
    private String status;
    
    // 创建时间
    private Date createdAt;
    
    // 更新时间
    private Date updatedAt;
    
    // 从PropRental和额外信息构建DTO
    public static PropRentalDto fromPropRental(PropRental rental, 
                                              String propName, 
                                              String propDescription, 
                                              BigDecimal propPrice, 
                                              String propCoverImage) {
        PropRentalDto dto = new PropRentalDto();
        dto.setId(rental.getId());
        dto.setPropId(rental.getPropId());
        dto.setPropName(propName);
        dto.setPropDescription(propDescription);
        dto.setPropPrice(propPrice);
        dto.setPropCoverImage(propCoverImage);
        dto.setUserId(rental.getUserId());
        dto.setStartDate(rental.getStartDate());
        dto.setEndDate(rental.getEndDate());
        dto.setTotalPrice(rental.getTotalPrice());
        dto.setStatus(rental.getStatus());
        dto.setCreatedAt(rental.getCreatedAt());
        dto.setUpdatedAt(rental.getUpdatedAt());
        return dto;
    }
}
