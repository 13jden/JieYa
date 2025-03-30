package com.example.common.adminDto;

import com.alibaba.fastjson.JSONArray;
import com.example.common.pojo.VenueImage;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class VenueDto {
    private Integer id;
    private String name;
    private String content;
    private String location;
    private BigDecimal price;
    private Integer capacity;
    private Date createdAt;
    private Date updatedAt;
    private JSONArray tags;
    
    // 封面图片
    private String  coverImage;
    
    // 所有图片列表（包括封面）
    private List<ImageDto> imageList;
}
