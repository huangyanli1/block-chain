package com.block.chain.controller;



import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.entity.PrimaryAddressEntity;
import com.block.chain.service.*;
import com.block.chain.utils.PageResult;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.CurrencyTypeVO;
import com.block.chain.vo.CurrencyVO;
import com.block.chain.vo.QuotationParamerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Currency configuration controller
 * @author michael
 * @date 2022/10/10
 */

@RestController
@RequestMapping("/block")
public class BlockController {

    @Autowired
    private CurrencyTypeService currencyTypeService;

    @Autowired
    private CurrencyChainInfoService currencyChainInfoService;

    @Autowired
    private PrimaryAddressService primaryAddressService;

    @Autowired
    private CurrencyPriceService currencyPriceService;

    @Autowired
    private CompanyAddressService companyAddressService;

    @Autowired
    private SymbolSlugInfoService symbolSlugInfoService;

    @Autowired
    private QuotationHourService quotationHourService;

    @Autowired
    private CurrencyRateService currencyRateService;

    @Autowired
    private MongoDBService mongoDBService;



    /**
     * 获取币种配置信息
     * @return
     */
    @GetMapping("getCurrencyTypeList")
    public ResponseData<PageResult> getCurrencyTypeList(@RequestParam(required = false) Long id,@RequestParam(required = false) Integer isBuy,@RequestParam(required = false) Integer isExchange,@RequestParam Integer currencyStatus,@RequestParam(required = false) String net, @RequestParam String symbol, @RequestParam Integer coinType, @RequestParam Integer page, @RequestParam Integer pageSize){
        return currencyTypeService.getCurrencyTypeList(id,isBuy,isExchange,currencyStatus,net,symbol,coinType,page,pageSize);
    }


    /**
     * 保存币种配置
     * @param vo 币种配置信息
     * @return
     */
    @PostMapping("saveCurrencyType")
    public ResponseData<Long> saveCurrencyType(@RequestBody CurrencyTypeVO vo){
        return currencyTypeService.saveCurrencyType(vo);
    }


    /**
     * 币种配置删除
     * @param list 需删除的币种配置Id
     * @return
     */
    @PostMapping("deleteCurrencyType")
    public ResponseData deleteCurrencyType(@RequestBody List<String> list){
        return currencyTypeService.deleteCurrencyType(list);
    }

    /**
     * 币种配置 - 币种配置修改
     * @param vo 配置修改参数
     * @return
     */
    @PostMapping("updateCurrencyType")
    public ResponseData  updateCurrencyType(@RequestBody CurrencyTypeVO vo){
//        CurrencyTypeVO vos = new CurrencyTypeVO();
//        Long id = vo.getId() == null ? null : Long.valueOf(vo.getId());
//        BeanUtils.copyProperties(vo, vos);
//        vos.setId(id);
       return  currencyTypeService.updateCurrencyType(vo);
    }


    /**
     * 获取币种配置详情信息
     * @return
     */
    @GetMapping("getCurrencyTypeById")
    public ResponseData<CurrencyTypeEntity> getCurrencyTypeById(@RequestParam  String id){
        return  currencyTypeService.getCurrencyTypeById(id);
    }



    /**
     * 币种配置 - 修改币种上线状态
     * @param vo 配置修改参数(id和币种状态)
     * @return
     */
    @PostMapping("updateCurrencyStatus")
    public ResponseData  updateCurrencyStatus(@RequestBody  CurrencyTypeVO vo){
        return  currencyTypeService.updateCurrencyStatus(vo);
    }


    /**
     * 获取所有的公链信息
     * @return
     */
    @GetMapping("getAllChainInfo")
    public ResponseData  getAllChainInfo(){
        return currencyChainInfoService.getAllChainInfo();
    }



    /**
     * 用户地址录入
     * @param list 地址信息录入
     * @return
     */
    @CrossOrigin
    @PostMapping("savePrimaryAddress")
    public ResponseData savePrimaryAddress(@RequestBody List<PrimaryAddressEntity> list){
//        response.setHeader("Access-Control-Allow-Origin", "*");
        return primaryAddressService.savePrimaryAddress(list);
    }
    /**
     * 获取公链下所有的地址信息
     * @param net
     * @return
     */
    @GetMapping("/getAllPrimaryAddress")
    public ResponseData getAllPrimaryAddress(@RequestParam String net){
        return primaryAddressService.getAllPrimaryAddress(net);
    }


    /**
     * 按symbol（字符串用逗号分隔）筛选价格表中币种的实时价格
     * @param symbols
     * @return
     */
    @CrossOrigin
    @GetMapping("getSymbolPriceList")
    public ResponseData  getSymbolPriceList(@RequestParam String symbols){
        return currencyPriceService.getSymbolPriceList(symbols);
    }



    /**
     * symbol  - slug 信息录入接口
     * @param list
     * @return
     */
    @PostMapping("saveSymbolSlugInfo")
    public ResponseData saveSymbolSlugInfo(@RequestBody List<JSONObject> list) {
        return symbolSlugInfoService.saveSymbolSlugInfo(list);
    }

    /**
     * 获取所有可兑换币种
     * @return
     */
    @CrossOrigin
    @GetMapping("getAllExchangeList")
    public ResponseData  getAllExchangeList(){
        return  currencyTypeService.getAllExchangeList();
    }


    /**
     * 所有币种小时K线行情录入
     */
    @GetMapping("getQuotationHour")
    public void getQuotationHour(){
        quotationHourService.saveYesterdayQuotationHour();
    }


    /**
     * 补全交易记录中的coinPrice,coinValue字段
     * coinPrice
     * coinValue
     */
    @GetMapping("addCompletionTransaction")
    public void addCompletionTransaction(){
        quotationHourService.addCompletionTransaction();
    }


    /**
     * 汇率集成
     */
    @GetMapping("integratedRate")
    public void integratedRate(){
        currencyRateService.integratedRate();
    }

    /**
     * 获取法币汇率，需要接入外部数据
     */
    @GetMapping("getFiatRate")
    public ResponseData getFiatRate(@RequestParam String sourceCurrency,@RequestParam String quoteCurrency){
        return currencyRateService.getFiatRate(sourceCurrency,quoteCurrency);
    }

    /**
     * 获取所有监控了汇率的法币列表
     */
    @GetMapping("getFiatList")
    public ResponseData getFiatList(){
        return currencyRateService.getFiatList();
    }


    /**
     * 获取所有法币，并集成法币汇率 - 汇率集成的补充接口
     */
    @GetMapping("integratedSupplementaryRate")
    public ResponseData integratedSupplementaryRate(){
        return currencyRateService.integratedSupplementaryRate();
    }


    /**
     * 根据公链返回指定业务的所有地址 - 分页
     * @param net 公链
     * @param page 页码
     * @param pageSize 每页显示数量
     * @return
     */
    @GetMapping("getAddressList")
    public ResponseData getAddressList(@RequestParam String net, @RequestParam Integer page, @RequestParam Integer pageSize){
        return mongoDBService.getAddressByNet(net,page,pageSize);
    }


    /**
     * 获取所有监控了价格的数字货币列表
     * @return
     */
    @GetMapping("getALLCurrencyPrice")
    public ResponseData getALLCurrencyPrice(){
        return currencyRateService.getALLCurrencyPrice();
    }




    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    @GetMapping("exchangeRate")
    public ResponseData exchangeRate(@RequestParam String target){
        return currencyRateService.exchangeRate(target);
    }



    /**
     * 更新所有法币对应的美元汇率 - 通过蜜蜂查进行更新集成
     * @return
     */
    @GetMapping("mifengchaExchangeRate")
    public ResponseData mifengchaExchangeRate(){
        return currencyRateService.mifengchaExchangeRate();
    }


    @GetMapping("sendFiatRateMessage")
    public ResponseData sendFiatRateMessage(){
        return currencyRateService.sendFiatRateMessage();
    }

    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    @CrossOrigin
    @GetMapping("getSymbolsRateList")
    public ResponseData getSymbolsRateList(@RequestParam String symbols){
        return currencyRateService.getSymbolsRateList(symbols);
    }

    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    @CrossOrigin
    @GetMapping("getSymbolsRate")
    public ResponseData getSymbolsRate(@RequestParam String symbol){
        return currencyRateService.getSymbolsRate(symbol);
    }



    @GetMapping("addAddressToRedis")
    public ResponseData addAddressToRedis() throws Exception{
        return primaryAddressService.addAddressToRedis();
    }

    @GetMapping("getAddressToRedis")
    public ResponseData getAddressToRedis(@RequestParam String net,@RequestParam String address) throws Exception{
        return primaryAddressService.getAddressToRedis(net,address);
    }

    @PostMapping("putAddressToRedis")
    public ResponseData putAddressToRedis(@RequestBody PrimaryAddressEntity entity) throws Exception{
        return primaryAddressService.putAddressToRedis(entity);
    }

    @PostMapping("getQuotationList")
    public ResponseData getQuotationList(@RequestBody List<QuotationParamerVO> list){
        return quotationHourService.getQuotationList(list);
    }

    /**
     * 根据传入主币和代币分别获取主币代币价格，以及主币和代币的汇率
     * @param net
     * @param symbol
     * @return
     */
    @CrossOrigin
    @GetMapping("getNetSymbolRate")
    public ResponseData  getNetSymbolRate(@RequestParam String net,@RequestParam String symbol){
        return currencyPriceService.getNetSymbolRate(net,symbol);
    }

    @CrossOrigin
    @GetMapping("getSymbolRate")
    public ResponseData  getSymbolRate(@RequestParam String contractAddress){
        return currencyPriceService.getNetSymbolRate(contractAddress);
    }

}
