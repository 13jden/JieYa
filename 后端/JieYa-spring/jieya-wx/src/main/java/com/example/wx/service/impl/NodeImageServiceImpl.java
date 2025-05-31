package com.example.wx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.pojo.NodeImage;
import com.example.common.pojo.NodePost;
import com.example.wx.mapper.NodeImageMapper;
import com.example.wx.mapper.NodePostMapper;
import com.example.wx.service.NodeImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Service
public class NodeImageServiceImpl extends ServiceImpl<NodeImageMapper, NodeImage> implements NodeImageService {

    @Autowired
    private NodeImageMapper nodeImageMapper;
    
    @Autowired
    private NodePostMapper nodePostMapper;
    
    @Value("${upload.tempPath}")
    private String tempPath;
    

}
