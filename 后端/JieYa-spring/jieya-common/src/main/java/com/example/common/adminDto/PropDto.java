package com.example.common.adminDto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class PropDto {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean available;
    private Date createdAt;
    private Date updatedAt;
    
    // 普通图片列表
    private List<ImageDto> imageList;
    
    // 详情图片列表
    private List<ImageDto> detailImageList;
    
    // 封面图片URL
    private String coverImage;
}