package com.block.chain.service.impl;

import com.block.chain.entity.WalletCurrencyTransactionEntity;
import com.block.chain.mapper.CompanyAddressMapper;
import com.block.chain.mapper.WalletCurrencyTransactionMapper;
import com.block.chain.service.CompanyAddressService;
import com.block.chain.service.WalletCurrencyTransactionService;
import com.block.chain.utils.*;
import com.block.chain.vo.bussiness.OrderNotificationVO;
import com.block.chain.vo.bussiness.OrdersReqVO;
import com.block.chain.vo.convert.WalletCurrencyTransactionVO;
import com.block.chain.vo.management.TransactionParamerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 币币兑换交易订单实现类
 */
@Slf4j
@Service("walletCurrencyTransactionService")
public class WalletCurrencyTransactionServiceImpl extends ServiceImpl<WalletCurrencyTransactionMapper, WalletCurrencyTransactionEntity> implements WalletCurrencyTransactionService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private CompanyAddressService companyAddressService;

    @Autowired
    private CompanyAddressMapper companyAddressMapper;

    @Autowired
    private WalletCurrencyTransactionMapper walletCurrencyTransactionMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${BUSSINESS.BUSSINESSURL}")
    private String bussinessUrl;

    @Value("${BUSSINESS.ORDERNOTIFICATIONURL}")
    private String orderNotificationUrl;

    @Value("${BUSSINESS.SAVEBUYOREXCHANGEURL}")
    private String saveBuyOrExchangeUrl;


    /**
     * 币币兑换订单录入接口 - 对接去中心化交易所
     * @param entity 订单交易信息
     * @return
     */
    public ResponseData saveCurrencyTransactionInfo(WalletCurrencyTransactionEntity entity){
        RandomGUID myGUID = new RandomGUID();
        String guid = myGUID.toString();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, BigDecimal> priceMap = commonService.getPriceMap();
        String entrySymbol = entity.getEntrySymbol();
        String outSymbol = entity.getOutSymbol();
        try {
            String greenTime = DatesUtil.getGreenTime();
            Long time = Long.valueOf(greenTime);
//            Date greenDate = format.parse(greenTime);
            entity.setCreateTime(time/1000);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        entity.setOrderId(guid);
        entity.setBusinessId(Constant.BUSINESS_ONE);//业务线Id，默认 1：Ulla 钱包
        entity.setServiceFeeAmount(new BigDecimal(0));
        //2: broadcasting 正在广播 - 默认状态
        //3: unconfirmed 已经广播，尚未确认
        //4: confirmed 交易已经确认
        entity.setTransactionStatus(2);
        //获取币币兑换交易时，入账币种和出账币种的价格
        if(StringUtils.isNotEmpty(entrySymbol)&&StringUtils.isNotEmpty(outSymbol)){
            BigDecimal entryPrice = priceMap.get(entrySymbol);
            BigDecimal outPrice = priceMap.get(outSymbol);
            entity.setEntryPrice(entryPrice);
            entity.setOutPrice(outPrice);
        }
        int size  = walletCurrencyTransactionMapper.insert(entity);
        if(size <  0){
            return ResponseData.fail("录入订单失败！");
        }else{
            try {
                OrdersReqVO vo = new OrdersReqVO();
                vo.setOutOrderNo(entity.getOrderId());
                vo.setBusinessData(entity.getBusinessData());
                vo.setType(Constant.BUSSINESS_EXCHANGE);
                String url = bussinessUrl+saveBuyOrExchangeUrl;
                HttpEntity<OrdersReqVO> request = new HttpEntity<>(vo,null);
                Object result = restTemplate.postForObject(url,request,Object.class);
//                String s = "";
            }catch (Exception e){
                e.printStackTrace();
                log.error("币币兑换订单生成时回调业务保存接口执行失败"+ e);
                return null;
            }
        }
        return ResponseData.ok("录入订单成功");
    }


    /**
     * 兑换列表查询接口
     * @param vo 查询参数
     * @return
     */
    public ResponseData getCurrencyTransactionList(TransactionParamerVO vo){
        List<WalletCurrencyTransactionVO>  voList = new ArrayList<>();
        Integer currPage = vo.getPage();
        Integer page = (vo.getPage()-1)*vo.getPageSize();
        vo.setPage(page);
        if(CollectionUtils.isEmpty(vo.getOrderList())){
            vo.setOrderList(null);
        }
        List<WalletCurrencyTransactionEntity> list = walletCurrencyTransactionMapper.getCurrencyTransactionList(vo);
        if(CollectionUtils.isNotEmpty(list)){
            for(WalletCurrencyTransactionEntity entity : list){
                WalletCurrencyTransactionVO tran = new WalletCurrencyTransactionVO();
                BeanUtils.copyProperties(entity, tran);
                String outAmount = entity.getOutAmount() == null ? "" : entity.getOutAmount().stripTrailingZeros().toPlainString();
                tran.setOutAmount(outAmount);
                voList.add(tran);
            }
        }
        int count = walletCurrencyTransactionMapper.getCurrencyTransactionCount(vo);
        PageUtils pageUtils = new PageUtils(voList,count,vo.getPageSize(),currPage);
        return ResponseData.ok(pageUtils);
    }

    /**
     * 币币兑换交易信息 - 详情查询接口
     * @param id 币币兑换订单ID
     * @return
     */
    public ResponseData getCurrencyTransactionById(String id){
        TransactionParamerVO vo = new TransactionParamerVO();
        WalletCurrencyTransactionVO tran = new WalletCurrencyTransactionVO();
        if(StringUtils.isNotEmpty(id)){
            vo.setOrderId(id);
            List<WalletCurrencyTransactionEntity> list = walletCurrencyTransactionMapper.getCurrencyTransactionInfo(vo);
            if(CollectionUtils.isNotEmpty(list)){
                WalletCurrencyTransactionEntity entity = list.get(0);
                BeanUtils.copyProperties(entity, tran);
                String outAmount = entity.getOutAmount() == null ? "" : entity.getOutAmount().stripTrailingZeros().toPlainString();
                tran.setOutAmount(outAmount);
            }
        }
        return ResponseData.ok(tran);

    }


    /**
     * 获取所有状态为broadcasting币币兑换交易数据
     * @return
     */
    public ResponseData getBroadcastingList(){
        List<WalletCurrencyTransactionEntity> list = walletCurrencyTransactionMapper.getBroadcastingList();
        return ResponseData.ok(list);
    }

    /**
     * 订单状态修改
     * @param list
     * @return
     */
    public ResponseData updateCurrencyTransaction(List<WalletCurrencyTransactionEntity> list) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date greenDate = new Date();
        String greenTime = DatesUtil.getGreenTime();
        Long time = Long.valueOf(greenTime);
////            Date greenDate = format.parse(greenTime);
//        entity.setCreateTime(time/1000);
//        try {
//            String greenTime = DatesUtil.getGreenTime();
//            greenDate = format.parse(greenTime);
//        }catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
        List<WalletCurrencyTransactionEntity> tranList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
            for(WalletCurrencyTransactionEntity entity : list){
                WalletCurrencyTransactionEntity vo = new WalletCurrencyTransactionEntity();
                Long  id = entity.getId();
                if(id != null ) {
                    vo.setId(id);
                    vo.setTransactionStatus(entity.getTransactionStatus());
                    vo.setOutAmount(entity.getOutAmount());
                    vo.setUpdateTime(time/1000);
                    vo.setGasFee(entity.getGasFee());
                    tranList.add(vo);
                }
            }
        }
        Boolean isUpdate = this.updateBatchById(tranList);
        if(isUpdate){
            List<Long> idList = new ArrayList<>();
            TransactionParamerVO vo = new TransactionParamerVO();
            List<String> orderList = new ArrayList<>();

            //拿到需要查询的数据主键ID
            for (WalletCurrencyTransactionEntity entity : tranList){
                Long id = entity.getId();
                idList.add(id);
            }
            if(CollectionUtils.isNotEmpty(idList)){
                vo.setIdList(idList);
                //获取订单表中的数据
                List<WalletCurrencyTransactionEntity> transactionList = walletCurrencyTransactionMapper.getCurrencyTransactionInfo(vo);
                OrderNotificationVO order = new OrderNotificationVO();
                for(WalletCurrencyTransactionEntity entity : transactionList){
                   orderList.add(entity.getOrderId());
                }
                order.setOrderNoList(orderList);
                order.setOrderNoType(Constant.ORDER_NO_TYPE);

                try {
                    String url = bussinessUrl+orderNotificationUrl;
                    HttpEntity<OrderNotificationVO> request = new HttpEntity<>(order,null);
                    Object result = restTemplate.postForObject(url,request,Object.class);
//                  String s = "";
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("调用业务消息推送执行失败="+ e);
                    return null;
                }
            }
        }
        return ResponseData.ok("币币兑换订单状态修改成功");

    }














//    /**
//     * 币币兑换 - 订单录入接口
//     * @param entity
//     * @return
//     */
//    public ResponseData saveOrderInfo(WalletCurrencyTransactionEntity entity){
////        Map<String, BigDecimal> price = commonService.getPriceMap();
//        String entryToAddr = entity.getEntryToAddr();//入账交易接收方地址，及入账交易公司地址
//        String entryNet = entity.getEntryNet();//入账交易公链
//        String entrySymbol = entity.getEntrySymbol();
//        String outSymbol = entity.getOutSymbol();
//        //获取此刻汇率
//        BigDecimal nowRate = commonService.getExchangeRate(entrySymbol,outSymbol);
//        BigDecimal estimateRate = entity.getEstimateRate();//估算汇率
//        BigDecimal preciseRate = entity.getPreciseRate();//精准汇率
//        //计算偏差 ： （旧的汇率 减去 新的汇率）除以 旧的汇率 = 偏差滑点
//        //判断偏差滑点是否大于1  isPoint 为true 偏差滑点在百分之一以内 为false 偏差滑点在百分之一以外
//        //校验1：客户端信息校验-根据传入汇率进行滑点校验(获取现阶段入账币种价格/出账币种价格=此刻汇率 ) 此刻汇率和点击交易按钮时汇率比较 判断偏差是否在1%内）
//        Boolean isPoint = commonService.isSlipPoint(preciseRate,nowRate);
//        if(!isPoint){
//            ResponseData.fail("入账币出账币滑点偏差在超过预期值！");
//        }
//        //校验2：判断客户端传过来的地址是否是公司地址 - 同时从业务那边判断该公司地址是否能收取入账交易的币种
//        Boolean isCompanyAddr = isCompanyAddress(entryToAddr,entryNet);
//        if(!isCompanyAddr){//如果不是公司地址，则返回错误信息
//            ResponseData.fail("入账地址非公司地址");
//        }
//        //校验3： 判断客户端传过来的diff（入账交易） 是否在后端管理系统设置的交易大小值范围内（阈值判断）
//        //暂无，需要业务提供
//        walletCurrencyTransactionMapper.insert(entity);
//        return ResponseData.ok("订单录入成功");
//    }
//
//
//    /**
//     * 判断地址是否为公司地址
//     * @param address 地址
//     * @param net 公链
//     * @return
//     */
//    public Boolean  isCompanyAddress(String address ,String net){
//        Boolean isCompanyAddress = false;
//        Map<String, Object> columnMap = new HashMap<String , Object>();
//        columnMap.put("net", net);
//        List<CompanyAddressEntity> addressList= companyAddressMapper.selectByMap(columnMap);
//        //该地址存在公司地址池中，则表明是公司地址
//        if(CollectionUtils.isNotEmpty(addressList)){
//            for(CompanyAddressEntity entity : addressList){
//                String ads = entity.getAddress();
//                String comNet =entity.getNet();
//                if(Constant.BTC.equals(comNet)||Constant.TRX.equals(comNet)){
//                    if(address.equals(ads)){
//                        isCompanyAddress = true ;
//                    }
//                }else{
//                    String newAds = ads.toLowerCase();
//                    String newAddress = address.toLowerCase();
//                    if(newAddress.equals(newAds)){
//                        isCompanyAddress = true ;
//                    }
//                }
//            }
//        }
//        return isCompanyAddress;
//    }
//








}