package com.example.wx.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.WxDto.NoteDto;
import com.example.common.pojo.Comment;
import com.example.common.pojo.Node;
import com.example.common.pojo.NodeImage;
import com.example.common.pojo.User;
import com.example.common.utils.CopyTools;
import com.example.wx.mapper.FocusMapper;
import com.example.wx.mapper.NodeImageMapper;
import com.example.wx.mapper.NodeMapper;
import com.example.wx.mapper.UserMapper;
import com.example.wx.service.NodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Service
public class NodeServiceImpl extends ServiceImpl<NodeMapper, Node> implements NodeService {

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FocusMapper focusMapper;

    @Autowired
    private NodeImageMapper nodeImageMapper;

    @Value("${host.url}")
    private String hostUrl;

    @Override
    public Page<NoteDto> getList(String userId,int pageNum, int pageSize) {
        // 创建分页对象
        Page<Node> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Node::getPostTime);
        Page<Node> nodePage = nodeMapper.selectPage(page, queryWrapper);

        // 准备结果页
        Page<NoteDto> dtoPage = new Page<>();
        org.springframework.beans.BeanUtils.copyProperties(nodePage, dtoPage, "records");

        List<NoteDto> nodeDtos = new ArrayList<>();
        // 处理每个笔记转换为DTO并设置附加信息
        for (Node node : nodePage.getRecords()) {
            NoteDto nodeDto = CopyTools.copy(node, NoteDto.class);

            // 处理封面图片路径
            if (nodeDto.getCoverImage() != null && !nodeDto.getCoverImage().isEmpty()) {
                nodeDto.setCoverImage(hostUrl + "/images/note/" + nodeDto.getCoverImage());
            }

            // 设置作者信息
            if (node.getUserId() != null) {
                User user = userMapper.selectById(node.getUserId());
                if (user != null) {
                    user.setAvatar(hostUrl + "/images/avatar/" + user.getAvatar());
                    nodeDto.setUser(user);
                }
                nodeDto.setUser(user);
                if(userId==user.getUserId()){
                    nodeDto.setIsFocus(0);
                }else{
                    if(focusMapper.checkUserFocus(userId,user.getUserId())>0){
                        nodeDto.setIsFocus(1);
                    }else{
                        nodeDto.setIsFocus(-1);
                    }
                }
            }

            nodeDtos.add(nodeDto);
        }

        dtoPage.setRecords(nodeDtos);
        return dtoPage;
    }


    @Override
    public NoteDto getDetail(String userId,String id) {
        Node node = nodeMapper.selectById(id);
        NoteDto noteDto = CopyTools.copy(node, NoteDto.class);
        List<NodeImage> nodeImages = nodeImageMapper.selectByNodeIdOrderBySort(id);
        System.out.println(nodeImages);
        User user = userMapper.selectById(node.getUserId());
        if (node.getCoverImage() != null && !node.getCoverImage().isEmpty()) {
            node.setCoverImage(hostUrl + "/images/note/" + node.getCoverImage());
        }
        for (NodeImage nodeImage : nodeImages) {
            System.out.println(nodeImage.getNodeId());
            nodeImage.setImagePath(hostUrl + "/images/note/" + nodeImage.getImagePath());
        }
        user.setAvatar(hostUrl+"/images/avatar/"+user.getAvatar());
        noteDto.setNoteImages(nodeImages);
        noteDto.setUser(user);
        if(userId==user.getUserId()){
            noteDto.setIsFocus(0);
        }else{
            if(focusMapper.checkUserFocus(userId,user.getUserId())>0){
                noteDto.setIsFocus(1);
            }else{
                noteDto.setIsFocus(-1);
            }
        }
        return noteDto;
    }

    @Override
    public Page<NoteDto> searchByKeyword(String userId, String keyword, int pageNum, int pageSize) {
        // 创建分页对象
        Page<Node> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Node> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加模糊查询条件，搜索标题和内容
        queryWrapper.like(Node::getTitle, keyword)
                   .or()
                   .like(Node::getContent, keyword);
                   
        queryWrapper.orderByDesc(Node::getPostTime);
        Page<Node> nodePage = nodeMapper.selectPage(page, queryWrapper);

        // 准备结果页
        Page<NoteDto> dtoPage = new Page<>();
        org.springframework.beans.BeanUtils.copyProperties(nodePage, dtoPage, "records");

        List<NoteDto> nodeDtos = new ArrayList<>();
        // 处理每个笔记转换为DTO并设置附加信息
        for (Node node : nodePage.getRecords()) {
            NoteDto nodeDto = CopyTools.copy(node, NoteDto.class);

            // 处理封面图片路径
            if (nodeDto.getCoverImage() != null && !nodeDto.getCoverImage().isEmpty()) {
                nodeDto.setCoverImage(hostUrl + "/images/note/" + nodeDto.getCoverImage());
            }

            // 设置作者信息
            if (node.getUserId() != null) {
                User user = userMapper.selectById(node.getUserId());
                if (user != null) {
                    user.setAvatar(hostUrl + "/images/avatar/" + user.getAvatar());
                    nodeDto.setUser(user);
                }
                
                if(userId.equals(user.getUserId())){
                    nodeDto.setIsFocus(0);
                }else{
                    if(focusMapper.checkUserFocus(userId,user.getUserId())>0){
                        nodeDto.setIsFocus(1);
                    }else{
                        nodeDto.setIsFocus(-1);
                    }
                }
            }

            nodeDtos.add(nodeDto);
        }

        dtoPage.setRecords(nodeDtos);
        return dtoPage;
    }
}
