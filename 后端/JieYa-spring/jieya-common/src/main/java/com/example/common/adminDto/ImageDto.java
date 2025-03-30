package com.example.common.adminDto;

import lombok.Data;

@Data
public class ImageDto {
    private Integer id;
    private String imageUrl;
    private int sortOrder;
    private String isDetail;

}
