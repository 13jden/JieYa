package com.example.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author dzk
 * @since 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NodeImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String imagePath;
    @TableField("node_id")
    private String nodeId;
    private Integer sort; // 新增排序字段
    
    public NodeImage() {}
    
    public NodeImage(String imagePath, String nodeId) {
        this.imagePath = imagePath;
        this.nodeId = nodeId;
        this.sort = 1; // 默认为1
    }
    
    public NodeImage(String imagePath, String nodeId, Integer sort) {
        this.imagePath = imagePath;
        this.nodeId = nodeId;
        this.sort = sort;
    }


}
