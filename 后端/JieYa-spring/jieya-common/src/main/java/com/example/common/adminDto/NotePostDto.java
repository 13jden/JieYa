package com.example.common.adminDto;

import com.example.common.pojo.NodeImage;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Data
public class NotePostDto {
    private String id;
    private String title;
    private String content;
    private String tags;
    private Integer category;
    private String coverImage;
    private Date postTime;
    private String userId;
    private Integer status;
    private List<NodeImage> images;
}   