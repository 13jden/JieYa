package com.example.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2025-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PropImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer propId;

    private String imageUrl;

    @TableField("is_detail")
    private Boolean isDetail;

    private Integer sortOrder;

    private Date createdAt;

    public void setIsDetailFromString(String isDetailStr) {
        this.isDetail = "1".equals(isDetailStr);
    }
}
