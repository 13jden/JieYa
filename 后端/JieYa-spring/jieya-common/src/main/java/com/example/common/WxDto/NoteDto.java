package com.example.common.WxDto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.common.pojo.NodeImage;
import com.example.common.pojo.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class NoteDto {

    private String id;

    private String title;

    private String content;

    private String tags;

    private String coverImage;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer recommendCount;

    private Integer category;

    private User user;

    private List<NodeImage> noteImages;

    private Date postTime;

    private int isFocus;//0是自己，1是为关注，-1是未关注

//    private List<Comment> commentList;
}
