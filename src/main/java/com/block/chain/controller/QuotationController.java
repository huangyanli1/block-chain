package com.block.chain.controller;


import com.block.chain.service.CurrencyPriceService;
import com.block.chain.service.QuotationService;
import com.block.chain.utils.R;
import com.block.chain.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Currency configuration controller
 * @author michael
 * @date 2022/10/14
 */
@RestController
@RequestMapping("/quotation")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private CurrencyPriceService currencyPriceService;

    /**
     * 按条件筛选历史行情数据
     * @param symbol 币种符号
     * @param intervalType K线间隔
     * @param startTime 开始时间(时间戳)
     * @param endTime 结束时间(时间戳)
     * @return
     */
    @GetMapping("/getQuotationList")
    public ResponseData getQuotationList(@RequestParam String symbol, @RequestParam String intervalType,@RequestParam String startTime ,@RequestParam String endTime){
        return quotationService.getQuotationList(symbol,intervalType,startTime,endTime);
    }

    /**
     * 按条件筛选币种实时价格
     * @param symbol 币种符号
     * @return
     */
    @CrossOrigin
    @GetMapping("/getCurrencyPriceList")
    public ResponseData getCurrencyPriceList(@RequestParam String symbol){
        return currencyPriceService.getCurrencyPriceList(symbol);
    }


    /**
     * 判断数据库中是否存在某一张表
     * @param tableName
     * @return
     */
    @GetMapping("/getIsHaveTable")
    public R getIsHaveTable(@RequestParam String tableName){
        return quotationService.getIsHaveTable(tableName);
    }

    /**
     * 自动建表
     * @return
     */
    @GetMapping("automaticCreateTable")
    public R automaticCreateTable(@RequestParam String tableName){
        return  quotationService.automaticCreateTable(tableName);
    }

    /**
     * 获取币种价格
     * @param url 查询URL
     * @return
     */
    @GetMapping("/getCurrencyPrice")
    public R getCurrencyPrice(String url){
        return quotationService.getCurrencyPrice(url);
    }

}
