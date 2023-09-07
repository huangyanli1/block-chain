package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.entity.PrimaryAddressEntity;
import com.block.chain.entity.StatisticInfoEntity;
import com.block.chain.entity.WalletTransactionEntity;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.PrimaryAddressMapper;
import com.block.chain.mapper.StatisticInfoMapper;
import com.block.chain.mapper.WalletTransactionMapper;
import com.block.chain.service.PrimaryAddressService;
import com.block.chain.utils.Constant;
import com.block.chain.utils.PageUtils;
import com.block.chain.utils.ResponseData;
import com.block.chain.utils.redis.RedisUtil;
import com.block.chain.vo.AddressVO;
import com.block.chain.vo.EntriesVO;
import com.block.chain.vo.PrimaryBalanceVO;
import com.block.chain.vo.bussiness.OrdersReqVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;

import javax.xml.ws.Response;


@Service("primaryAddressService")
@Slf4j
public class PrimaryAddressServiceImpl extends ServiceImpl<PrimaryAddressMapper, PrimaryAddressEntity> implements PrimaryAddressService {

    @Autowired
    private PrimaryAddressMapper primaryAddressMapper;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private StatisticInfoMapper statisticInfoMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${EXTERNA.BalanceUrl}")
    private String balanceUrl;

    @Value("${EXTERNA.NewAddressUrl}")
    private String newAddressUrl;

    @Value("${EXTERNA.ISVALIDADDRESSURL}")
    private String ISVALIDADDRESSURL;


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取公链下所有的地址信息
     * @param net
     * @return
     */
    public ResponseData getAllPrimaryAddress(String net){
        List<PrimaryAddressEntity> list = primaryAddressMapper.getAllPrimaryAddress(net);
        return ResponseData.ok(list);
    }


    /**
     * 用户地址录入
     * @param list 地址信息
     * @return
     */
    public ResponseData savePrimaryAddress(List<PrimaryAddressEntity> list){
        List<PrimaryAddressEntity> primaryAddressList = new ArrayList<>();
        List<String>  chainList = currencyTypeMapper.getChainList();
        //录入时默认为0
        BigDecimal balance = new BigDecimal(0);
        for(PrimaryAddressEntity entity : list){
            String address = entity.getAddress();
            String net = entity.getNet();
            Integer entityBusinessId = Optional.ofNullable(entity.getBusinessId()).orElse(0);
//            Integer entityBusinessId = entity.getBusinessId() == null ? 0 :entity.getBusinessId();
//            Map<String, Object> columnMap = new HashMap<String , Object>();
//            columnMap.put("address", address);
//            columnMap.put("net", net);
//            List<PrimaryAddressEntity>  addressList= primaryAddressMapper.selectByMap(columnMap);
            List<PrimaryAddressEntity> addressList = primaryAddressMapper.getAddressListByNet(net,address);
            //0718新增功能 - 替换老钱包的前期准备
            if(CollectionUtils.isNotEmpty(addressList) && entityBusinessId == 4){
                PrimaryAddressEntity addressEntity = addressList.get(0);
                Integer businessId = Optional.ofNullable(addressEntity.getBusinessId()).orElse(0);
                if(businessId != 4){
                    addressEntity.setBusinessId(4);
                    primaryAddressMapper.updateById(addressEntity);
                }
            }
            if(CollectionUtils.isEmpty(addressList)){
                entity.setBalance(balance);
                entity.setIsInitialize(1);
                boolean isValid = this.getIsValid(entity);
                //isValid为TRUE时，地址准确性校验通过，新地址录入
                if(isValid&&chainList.contains(net)){
                    primaryAddressMapper.insert(entity);
                    primaryAddressList.add(entity);
                    putAddressToRedis(entity);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(primaryAddressList)){
            try {
                HttpEntity<List<PrimaryAddressEntity>> request = new HttpEntity<>(primaryAddressList,null);
                Object result = restTemplate.postForObject(newAddressUrl,request,Object.class);
            }catch (Exception e){
                e.printStackTrace();
                log.error("用户地址录入- 调用扫链接口执行失败"+ e);
                return null;
            }
        }
        return ResponseData.ok("地址录入成功");
    }

    /**
     * 校验传入地址是否准确
     * @param entity
     * @return
     */
    public boolean getIsValid(PrimaryAddressEntity entity){
        Boolean  isValid = true;
        String address = entity.getAddress();
        String net = entity.getNet();
        String url = ISVALIDADDRESSURL+"?address="+address+"&net="+net;
        Object result = restTemplate.getForObject(url,Object.class);
        JSONObject object = JSONObject.parseObject(JSONObject.toJSONString(result));
        Boolean isSuccess = Boolean.valueOf(object.get("success").toString());
        JSONObject data = JSONObject.parseObject(object.get("data").toString());
        if(!isSuccess){
            isValid = false;
        }else{
            isValid = data.getBoolean("isValid");
        }
        return isValid;
    }


    /**
     * 统计相关
     * @return
     */
    public ResponseData getStatementInfo(){
        //统计昨日数据，所以时间减一天
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Long time = cal.getTimeInMillis();

        StatisticInfoEntity entity = new StatisticInfoEntity();
        //获取币种所有信息
        List<CurrencyTypeEntity> currencyList = currencyTypeMapper.getCurrencyList();
        //获取所有地址信息，并按照net分组统计数量
        List<PrimaryAddressEntity> primaryAddressList = primaryAddressMapper.getPrimaryaddressList();
        //按net对地址信息进行分组
        Map<String, List<PrimaryAddressEntity>>  busACCMap = primaryAddressList.stream().collect(
                Collectors.groupingBy(PrimaryAddressEntity::getNet));
        //统计net下地址数量
        Map<String ,Integer> map = new HashMap<>();
        busACCMap.forEach((key,value)->{
            List<PrimaryAddressEntity> mapList = value;
            map.put(key,mapList.size());
        });
        //获取昨日入账交易记录
        List<WalletTransactionEntity> entriesList = this.getEntriesList(primaryAddressList);
        Integer entriesSize = entriesList.size();
        //计算昨日入账数量
        EntriesVO   entrie =getEntriesNumber(entriesList);

        //获取昨日提现交易记录
        List<WalletTransactionEntity> withdrawalList = this.getWithdrawalTransactionList(primaryAddressList);
        Integer withdrawalSize = withdrawalList.size();
        //计算昨日提现数量
        EntriesVO withdrawal =getEntriesNumber(withdrawalList);

        //统计价值和
        List<PrimaryBalanceVO> primaryList = new ArrayList<>();
        primaryList = getPrimaryBalanceList(primaryAddressList,primaryList);
        entity.setCurrencyInfo(JSONObject.toJSONString(currencyList));
        entity.setAddressInfo(JSONObject.toJSONString(map));
        entity.setCurrencyValue(JSONObject.toJSONString(primaryList));
        entity.setTransactionException("0");
        entity.setEntriesNumber(String.valueOf(entriesSize));
        entity.setEntriesInfo(JSONObject.toJSONString(entrie));
        entity.setWithdrawalNumber(String.valueOf(withdrawalSize));
        entity.setWithdrawalInfo(JSONObject.toJSONString(withdrawal));
        entity.setCreateTime(time);
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        List<StatisticInfoEntity> list = statisticInfoMapper.getStatisticInfoList(startTime,endTime);
        if(CollectionUtils.isEmpty(list)) {
            statisticInfoMapper.insert(entity);
        }
        return ResponseData.ok(entity);
    }

    //获取当天的开始时间
    public  Long getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        todayStart.add(Calendar.DAY_OF_MONTH, -1);
        todayStart.add(Calendar.HOUR_OF_DAY, -8);
        return todayStart.getTimeInMillis();
    }
    //获取当天的结束时间
    public  Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MILLISECOND, 999);
        todayEnd.add(Calendar.DAY_OF_MONTH, -1);
        todayEnd.add(Calendar.HOUR_OF_DAY, -8);
        return todayEnd.getTimeInMillis();
    }

    /**
     * 计算今日入账笔数
     * @return
     */
    public List<WalletTransactionEntity> getEntriesList(List<PrimaryAddressEntity> primaryAddressList){
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        List<String> addressList = new ArrayList<>();
        for(PrimaryAddressEntity address : primaryAddressList){
            if(StringUtils.isNotEmpty(address.getAddress())){
                addressList.add(address.getAddress());
            }
        }//获取昨日所有入账交易记录
        List<WalletTransactionEntity>  list = walletTransactionMapper.getEntriesTransactionList(addressList,startTime,endTime,null);
        return list;
    }

    /**
     * 计算今日提现笔数
     * @return
     */
    public List<WalletTransactionEntity> getWithdrawalTransactionList(List<PrimaryAddressEntity> primaryAddressList){
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        List<String> addressList = new ArrayList<>();
        for(PrimaryAddressEntity address : primaryAddressList){
            if(StringUtils.isNotEmpty(address.getAddress())){
                addressList.add(address.getAddress());
            }
        }
        //获取昨日提现交易记录
        List<WalletTransactionEntity>  list = walletTransactionMapper.getWithdrawalTransactionList(addressList,startTime,endTime,null);
        return list;
    }



    /**
     * 计算昨日入账，和提现数量 - 公用
     * @return
     */
    public EntriesVO getEntriesNumber(List<WalletTransactionEntity> list){
        EntriesVO vo = new EntriesVO();
        Map<String ,BigDecimal> primarysMap = new HashMap<>();
        Map<String ,BigDecimal> tokensMap = new HashMap<>();
        for(WalletTransactionEntity entity : list){
            //1：公链主币:2：ERC 20代币:3：OMNI 31代币:4：ERC 721代币:5：ERC 1155代币
            Integer coinType = entity.getCoinType();
            String symbol = entity.getSymbol();
            BigDecimal transactionValue = entity.getTransactionValue();
            //主币情况
            if(coinType == 1){
                if(primarysMap.containsKey(symbol)){
                    //获取该主币下剩余价格
                    BigDecimal prmBalance = primarysMap.get(symbol);
                    prmBalance = prmBalance.add(transactionValue);
                    primarysMap.put(symbol,prmBalance);
                }else{
                    //无该类型主币，则将主币和当前金额放入
                    primarysMap.put(symbol,transactionValue);
                }
            }else{//代币情况
                if(tokensMap.containsKey(symbol)){
                    BigDecimal tokenBalance = tokensMap.get(symbol);
                    tokenBalance = tokenBalance.add(transactionValue);
                    tokensMap.put(symbol,tokenBalance);
                }else{
                    tokensMap.put(symbol,transactionValue);
                }
            }
        }
        vo.setPrimarysMap(primarysMap);
        vo.setTokensMap(tokensMap);
        return vo;
    }






    /**
     * 处理所有用户地址 - 统计主币总价值和代币总价值
     * @param primaryAddressList 所有用户地址
     * @param primaryList 主币信息统计和
     * @return
     */
//    @Async
    public List<PrimaryBalanceVO> getPrimaryBalanceList(List<PrimaryAddressEntity> primaryAddressList ,List<PrimaryBalanceVO> primaryList ){
         //所有用户地址
         for(PrimaryAddressEntity entity : primaryAddressList){
             String address = entity.getAddress();
             String net = entity.getNet();
             Object data = this.requestAddressBalance(address,net);
             JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(data));

             JSONObject result = JSONObject.parseObject(JSONObject.toJSONString(json.get("data")));
             //查询地址下主币余额
             BigDecimal primaryBalance = result.getBigDecimal("balance");
             JSONArray tokens = JSONArray.parseArray(JSONObject.toJSONString(result.get("tokens")));
             //是否包含主币
             boolean isContainChain = primaryList.stream().filter(m->m.getNet().equals(net)).findAny().isPresent();
             //如果不包含主币，将主币信息录入到主币List中
             if(!isContainChain){
                 PrimaryBalanceVO vo = new PrimaryBalanceVO();
                 BigDecimal deci = new BigDecimal(0);
                 vo.setBalance(deci);
                 vo.setNet(net);
                 primaryList.add(vo);
             }

             for(PrimaryBalanceVO primary : primaryList){
                 //统计 - 主币类型
                 String primaryNet = primary.getNet();
                 BigDecimal balance = primary.getBalance();//统计 - 主币余额
                 Map<String,BigDecimal> tokensMap = primary.getTokensMap();//统计 - 主币下所有代币信息
                 if(net.equals(primaryNet)){
                     balance = balance.add(primaryBalance);
                     primary.setBalance(balance);
                     //地址下所有代币信息循环
                     for(Object token : tokens){
                         JSONObject tokenJson = JSONObject.parseObject(token.toString());
                         //代币名称
                         String tokenName = tokenJson.get("name") == null ? "":tokenJson.get("name").toString();
                         //查询地址下 代币余额
                         BigDecimal tokenBalance = tokenJson.get("balance") == null ? new BigDecimal(0) :new BigDecimal(tokenJson.get("balance").toString());
                         if(tokensMap == null){
                             tokensMap = new HashMap<>();
                         }
                         //如果map中存在该代币，则将代币余额拿出来加上查询地址下该代币的余额
                         if(tokensMap != null && tokensMap.containsKey(tokenName)){
                             BigDecimal mapBalance = tokensMap.get("tokenName") == null ? new BigDecimal(0) :tokensMap.get("tokenName");;
                             mapBalance = mapBalance.add(tokenBalance);
                             tokensMap.put(tokenName,mapBalance);
                         }else{
                             //如果map中不存在该代币，则将查询地址下的代币和余额保存进map
                             tokensMap.put(tokenName,tokenBalance);
                         }
                     }
                     //将处理完成的Map放入主币信息中记录
                     primary.setTokensMap(tokensMap);
                 }
             }
         }

         return primaryList;
    }

    /**
     * 获取地址下余额公用方法
     * @return
     */
    public Object requestAddressBalance(String address,String net){
        Object result = new Object();
        String url = "";
        try{
            url = balanceUrl+address+"&net="+net;
            result = restTemplate.getForObject(url,Object.class);
        } catch (Exception e) {
            log.error("统计时-获取地址下余额请求执行失败URL="+url);
            return result;
        }
        return result;
    }


    /**
     * 分页获取net下的用户地址
     * @param net 公链
     * @param page 页码
     * @param pageSize 每页展示的数量
     * @return
     */
    public ResponseData getAddressByNet(String net, Integer page, Integer pageSize){
        List<PrimaryAddressEntity> list = primaryAddressMapper.getAddressList(net,(page-1)*pageSize,pageSize);
        Integer listCount = primaryAddressMapper.getAddressCount(net);
        PageUtils pageUtils = new PageUtils(list,listCount,pageSize,page);
        return ResponseData.ok(pageUtils);
    }

    @Async
    public ResponseData addAddressToRedis()  {
        Long startTime = System.currentTimeMillis();
        System.out.println("用户地址已初始化开始时间"+startTime);
        List<String> list = currencyTypeMapper.getChainList();
        try {
            for(String net : list){
                String redisName = Constant.SCANBLOCK_USERADDRESS+net;
                RMap<String, String>  bucket = redissonClient.getMap(redisName);
                if (bucket.isExists()) {
                    redissonClient.getKeys().delete(redisName);
                }
                List<PrimaryAddressEntity> addressList = primaryAddressMapper.getAllPrimaryAddress(net);
                Map<String, String> resMap = new HashMap<>();
                for(PrimaryAddressEntity entity : addressList){
                    AddressVO vo = new AddressVO();
                    BeanUtils.copyProperties(entity, vo);
                    resMap.put(entity.getAddress(),JSONObject.toJSONString(vo));
                }
//                bucket.put(redisName, resMap);
                bucket.putAll(resMap);
            }

        }catch (Exception e){
            e.printStackTrace();
            log.error("用户地址已初始化进入redis失败"+ e);
            return null;
        }
        Long endTime = System.currentTimeMillis();
        System.out.println("用户地址已初始化结束时间"+endTime);
        System.out.println("用户地址已初始化进入redis！");
//        redissonClient.shutdown();
        return ResponseData.ok("用户地址已初始化进入redis！");
    }

    public ResponseData getAddressToRedis(String net,String address) throws Exception {

        String redisName = Constant.SCANBLOCK_USERADDRESS+net;
        RMap<String, String>  bucket = redissonClient.getMap(redisName);
        String value = bucket.get(address);
        JSONObject result = JSONObject.parseObject(value);
        return ResponseData.ok(result);
    }

    public ResponseData putAddressToRedis(PrimaryAddressEntity entity){
        String net = entity.getNet();
        String address = entity.getAddress();
        if(StringUtils.isNotEmpty(net)&&StringUtils.isNotEmpty(address)){
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(entity, vo);
            String redisName = Constant.SCANBLOCK_USERADDRESS+net;
            RMap<String, String>  bucket = redissonClient.getMap(redisName);
            bucket.put(address,JSONObject.toJSONString(vo));
        }
        return ResponseData.ok("redis录入成功");
    }
}