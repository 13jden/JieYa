package com.example.wx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时间段DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDto {
    // 时间段开始时间（格式：HH:mm）
    private String startTime;
    
    // 时间段结束时间（格式：HH:mm）
    private String endTime;
    
    // 时间段是否可用
    private Boolean available;
} 