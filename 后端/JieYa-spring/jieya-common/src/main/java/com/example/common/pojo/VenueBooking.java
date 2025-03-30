package com.example.common.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
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
 * @since 2025-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VenueBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private String id;

    private Integer venueId;

    private String userId;

    private Date endTime;

    private Date startTime;

    private BigDecimal totalPrice;

    private String status;

    private Date createdAt;

    private Date updatedAt;


}
