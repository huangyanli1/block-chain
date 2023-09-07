package com.block.chain.entity.mongo;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.json.JSONArray;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;


/**
 * @Document用于指定数据库的conllectionjav
 * @field用于指定数据库字段spring
 * @id用于标识主键mongodb
 * @GeneratedValue 自动生成id数据
 * @author qzz
 */
@Data
@Document(collection = "TRX")
public class TRX{

    /**
     * 主键标识,该属性的值会自动对应，mongodb的主键字段”_id“,如果该属性名就叫”id“,则该注解可以省略
     */
    @Id
    private String id;
    /**
     * @Field该属性对应 mongodb的字段的名字，如果一致，则无需该注解
     */
    private String address;
    private String net;
    private BigDecimal balance;
    private BigDecimal lastBlockNum;
    private List<Map<String,Object>> tokens;
    private Integer isScanned;
    private String symbol;

}