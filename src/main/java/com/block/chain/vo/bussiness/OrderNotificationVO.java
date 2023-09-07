package com.block.chain.vo.bussiness;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderNotificationVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 订单号
     */
    private List<String> orderNoList;


    /**
     * 订单类型 0-购买 1-兑换
     */
    private Integer orderNoType;
}
