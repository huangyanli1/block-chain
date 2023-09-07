package com.block.chain.vo.bussiness;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
public class OrdersReqVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String outOrderNo;
    private String businessData;
    private String type;

    public final static String UID = "uid";
    @AllArgsConstructor
    @Getter
    public enum typeEnum{
        EXCHANGE("exchange"),
        BUY("buy");
        private final String type;

        public static typeEnum of(String type) {

            for (typeEnum action : values()) {
                if (action.getType().equals(type)) {
                    return action;
                }
            }
            return null;
        }
    }
}
