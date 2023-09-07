package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.WalletTransactionEntity;
import com.block.chain.entity.mongo.TRX;
import com.block.chain.mapper.CurrencyPriceMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.mapper.TransactionBtcInfoMapper;
import com.block.chain.mapper.WalletTransactionMapper;
import com.block.chain.service.WalletTransactionService;
import com.block.chain.utils.*;
import com.block.chain.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;

/**
 * Wallet related service
 */
@Service("walletTransactionService")
@Slf4j
public class WalletTransactionServiceImpl extends ServiceImpl<WalletTransactionMapper, WalletTransactionEntity> implements WalletTransactionService {

    @Autowired
    private  WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${EXTERNA.BalanceUrl}")
    private String balanceURL;

    @Value("${EXTERNA.TxListUrl}")
    private String txListUrl;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public QuotationMapper quotationMapper;

    @Autowired
    private TransactionBtcInfoMapper transactionBtcInfoMapper;

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    public CommonService commonService;

    /**
     * 钱包交易记录保存
     * @param entity 钱包交易记录信息
     * @return ResponseData
     */
    public ResponseData saveWalletTransaction(WalletTransactionEntity entity){
        log.info("参数"+JSONObject.toJSONString(entity));
        RandomGUID myGUID = new RandomGUID();
        String guid = myGUID.toString();
        String txHash = entity.getTxHash();
        String net = entity.getNet();
        Long txCommitTime = entity.getTxCommitTime();
        Integer businessId = entity.getBusinessId();
        Integer transactionStatus = entity.getTransactionStatus();
        Integer coinType = entity.getCoinType();
        String fromAddr = entity.getFromAddr();
        String toAddr = entity.getToAddr();
        if(StringUtils.isEmpty(txHash)||StringUtils.isEmpty(net)||StringUtils.isEmpty(fromAddr)||StringUtils.isEmpty(toAddr)||businessId ==null||transactionStatus ==null||coinType==null||txCommitTime==null){
            log.info("txHash=="+txHash+"net=="+net+"fromAddr=="+fromAddr+"toAddr=="+toAddr+"businessId=="+businessId+"transactionStatus=="+transactionStatus+"coinType=="+coinType+"txCommitTime=="+txCommitTime);
            return   ResponseData.fail("缺少必填参数");
        }
        entity.setOrderId(guid);
        int size = walletTransactionMapper.insert(entity);
        if(size <= 0){
            return ResponseData.fail("钱包交易记录保存失败！");
        }
        return ResponseData.ok(entity);
    }


    /**
     * 钱包交易记录修改
     * @param entity
     * @return ResponseData
     */
    public ResponseData  updateWalletTransaction(WalletTransactionEntity entity){
        int size = walletTransactionMapper.updateById(entity);
        if(size <= 0 ){
            return ResponseData.fail("钱包交易记录修改失败！");
        }
        return ResponseData.ok(entity);
    }



    /**
     * 获取所有交易状态为unconfirmed的交易数据
     * @return ResponseData
     */
    public ResponseData getAllUnconfirmedList(){

        List<WalletTransactionEntity> list = walletTransactionMapper.getAllUnconfirmedList();

        return ResponseData.ok(list);
    }


    /**
     * 按条件筛选交易记录
     * @param addressList 交易地址列表
     * @return ResponseData
     */
    public ResponseData getTransactionList(List<String> addressList){
        List<WalletTransactionEntity> list = new ArrayList<>();
        if(CollectionUtils.isEmpty(addressList)){
            return ResponseData.fail("交易地址为空！");
        }
        list = walletTransactionMapper.getTransactionList(addressList);
        return ResponseData.ok(list);
    }

    /**
     * 获取地址下余额
     * @param address
     * @param net
     * @return ResponseData
     */
    public ResponseData getAddressBalance(String address,String net){
        AddressBalanceVO vo = new AddressBalanceVO();
        Object data = new Object();
        String url = "";
        try{
            url = balanceURL+address+"&net="+net;
            data = restTemplate.getForObject(url,Object.class);
        } catch (Exception e) {
            log.error("获取地址下余额请求执行失败URL="+url);
            return ResponseData.fail("请求链上信息失败");
        }
//        Object data =requestAddressBalance(address,net);

        JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(data));
        Boolean isSuccess = (Boolean)json.get("success");
        if(!isSuccess){
            return ResponseData.fail("请求链上信息失败");
        }
        JSONObject result = JSONObject.parseObject(JSONObject.toJSONString(json.get("data")));
        //查询地址下主币余额
        BigDecimal balance = result.getBigDecimal("balance") == null ? new BigDecimal(0) : result.getBigDecimal("balance");
        JSONArray tokens = JSONArray.parseArray(JSONObject.toJSONString(result.get("tokens")));
//        vo.setBalance(balance);
//        vo.setTokens(tokens);
        return ResponseData.ok(vo);
    }

    /**
     * 账号- 交易地址获取对应交易信息
     * @param params
     * @return ResponseData
     */
    public ResponseData getAccountChainTransaction(TransactionParamsVO params){
        List<TransactionChainVO> list = new ArrayList<>();
        TransactionPageVO pageVO = new TransactionPageVO();
        //获取锚点hash
        String txHash = params.getTxHash();
        String type = params.getType();//1、全部 2、划转 3、转账 4、收款
        Integer page = params.getPage();
        Integer pageSize = params.getPageSize();
        Long startTime = params.getStartTime();
        Long endTime = params.getEndTime();

        //公司地址
        List<TransactionAddressVO> companyAddressList = new ArrayList<>();
        //用户地址
        List<TransactionAddressVO> userAddressList = new ArrayList<>();
        //获取所有需要查询地址信息
        List<TransactionAddressVO> addressList = params.getAddressList();
        for(TransactionAddressVO vo : addressList){
            //用户地址
            if("1".equals(vo.getAddressType())){
                userAddressList.add(vo);
            }
            //公司地址
            if("2".equals(vo.getAddressType())){
                companyAddressList.add(vo);
            }
        }
        list = getAddressTransaction(userAddressList);

//        for(TransactionAddressVO vo : userAddressList){
//            List<TransactionChainVO> addressDataList = new ArrayList<>();
//            String net = vo.getNet();
//            String address = vo.getAddress();
//            //查询参数中的合约地址
//            String contractAddress = vo.getContractAddress();
//            if(StringUtils.isNotEmpty(net)&&StringUtils.isNotEmpty(address)){
//                //查询地址下所有交易记录，并处理
//                JSONArray data = this.getData(address,net);
//                for (Object obj : data) {
//                    JSONObject json = JSONObject.parseObject(obj.toString());
//                    TransactionChainVO chain = new TransactionChainVO();
//                    chain = this.getChainVO(json);
//                    addressDataList.add(chain);
//                }
//                //情况1:如果合约地址为空，表明是主币，拿取该地址下面coinType为主币类型的交易记录
//                //情况2:如果合约地址不为空，表明是代币，筛选该地址下交易记录中合约地址等于查询参数中的合约地址的交易记录
//                if(StringUtils.isNotEmpty(contractAddress)){//情况一
//                    for(TransactionChainVO chainVO : addressDataList){
//                        //交易记录中的合约地址
//                        String chainContractAddress = chainVO.getContractAddress();
//                        if(contractAddress.equals(chainContractAddress)){
//                            list.add(chainVO);
//                        }
//                    }
//                }else{//情况二
//                    for(TransactionChainVO chainVO : addressDataList){
//                        String coinType = chainVO.getCoinType();
//                        if("1".equals(coinType)){//主币交易记录
//                            list.add(chainVO);
//                        }
//                    }
//                }
//            }
//        }

        //对用户下所有地址的交易记录进行时间排序
        String firstTxHash = "";
        if(CollectionUtils.isNotEmpty(list)){
            list = list.stream().sorted(Comparator.comparing(TransactionChainVO::getBlockTime, Comparator.reverseOrder())).collect(Collectors.toList());
            if(startTime != null || endTime != null){
                if(startTime != null && endTime != null){
                    //如果起止时间都有，则按起止时间截止数据
                    list = list.stream().filter(s->s.getBlockTime()>=startTime && s.getBlockTime()<=endTime).collect(Collectors.toList());
                }
                if(startTime != null && endTime == null){
                    //如果只有开始时间，则按开始时间截止数据
                    list = list.stream().filter(s->s.getBlockTime()>=startTime).collect(Collectors.toList());
                }
                if(startTime == null && endTime != null){
                    //如果只有结束时间，则按结束时间截止数据
                    list = list.stream().filter(s->s.getBlockTime()<=endTime).collect(Collectors.toList());
                }
            }
            if(CollectionUtils.isNotEmpty(list)){
                firstTxHash = list.get(0).getTxHash();
            }
        }
        //当点击分页数据时比如点击第二页，获取第一页请求时交易数据，避免出现最新交易数据影响查询准确性
        boolean isHaveHash = false;
        if(StringUtils.isNotEmpty(txHash)){
            List<TransactionChainVO> hashList = new ArrayList<>();
            for(TransactionChainVO vo : list){
                if(txHash.equals(vo.getTxHash())){
                    isHaveHash = true;
                }
                if(isHaveHash){
                    hashList.add(vo);
                }
            }
            list = hashList;//将锚点以后数据赋值给list，保证数据准确性
            pageVO.setTxHash(txHash);
        }
        //处理后的交易记录
        List<TransactionChainVO> revertList = new ArrayList<>();
        //全部：查询交易记录情况一：全部(本人的所有地址属于from或者to，并且from不属于公户所有的地址)
        if(Constant.ONE.equals(type)){
            for(TransactionChainVO vo : list){
                List<String> fromAddr = vo.getFromAddr();
                List<String> toAddr = vo.getToAddr();
                //判断该交易记录from地址是否包含用户地址
                boolean isFrom = this.isHave(fromAddr,userAddressList);
                //判断该交易记录to地址是否包含用户地址
                boolean isTo = this.isHave(toAddr,userAddressList);
                //判断该交易记录from地址是否包含公司地址 1107业务和前段沟通后去掉该逻辑
//                boolean isFromCompany = this.isHave(fromAddr,companyAddressList);
                if(isFrom || isTo){
//                    if(!isFromCompany){
                        revertList.add(vo);
//                    }
                }
            }

        }
        //划转：查询交易情况二：本人的所有地址属于from，在加上公户的所有地址属于to
        if(Constant.TWO.equals(type)){
            for(TransactionChainVO vo : list){
                List<String> fromAddr = vo.getFromAddr();
                List<String> toAddr = vo.getToAddr();
                //判断该交易记录from地址是否包含用户地址
                boolean isFrom = this.isHave(fromAddr,userAddressList);
                //判断该交易记录to地址是否包含公司地址
//                boolean isToCompany = this.isHave(toAddr,companyAddressList);
//                if(isFrom && isToCompany){
                if(isFrom){
                    revertList.add(vo);
                }
            }
        }
        //转账：查询交易情况三：本人的所有地址属于from，并且to不属于公户所有的地址
        if(Constant.THREE.equals(type)){
            for(TransactionChainVO vo : list){
                List<String> fromAddr = vo.getFromAddr();
                List<String> toAddr = vo.getToAddr();
                //判断该交易记录from地址是否包含用户地址
                boolean isFrom = this.isHave(fromAddr,userAddressList);
                //判断该交易记录to地址是否包含公司地址
//                boolean isToCompany = this.isHave(toAddr,companyAddressList);
//                if(isFrom &&!isToCompany){
                if(isFrom){
                    revertList.add(vo);
                }
            }
        }
        //收款：查询交易情况四：本人的所有地址属于to中查询，并且from不属于公户所有的地址
        if(Constant.FOUR.equals(type)){
            for(TransactionChainVO vo : list){
                List<String> fromAddr = vo.getFromAddr();
                List<String> toAddr = vo.getToAddr();
                //判断该交易记录from地址是否包含用户地址
                boolean isTo = this.isHave(toAddr,userAddressList);
                //判断该交易记录to地址是否包含公司地址 1107业务和前段沟通后去掉该逻辑
//                boolean isFromCompany = this.isHave(fromAddr,companyAddressList);
                if(isTo){
//                    if(!isFromCompany){
                        revertList.add(vo);
//                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(revertList)){
            for(TransactionChainVO vo : revertList){
                String diff =vo.getDiff();
                BigDecimal diffNumber = new BigDecimal(diff);
                if(diffNumber.signum() ==-1){
                    diff = diff.replace("-","");
                    vo.setDiff(diff);
                }
                if(vo.getBlockTime() != null){
                    String block = String.valueOf(vo.getBlockTime());
                    Boolean isMillise = commonService.isMilliseSecond(block);
                    if(isMillise){
                        Long blcokTime = vo.getBlockTime();
                        vo.setBlockTime(blcokTime/1000);
                    }
                }
            }
        }
        PageUtils pageUtil = new PageUtils(revertList,page,pageSize);
        List<TransactionChainVO> pageList = (List<TransactionChainVO>) pageUtil.getList();
        BeanUtils.copyProperties(pageUtil, pageVO);
        pageVO.setList(pageList);
        //当第一次请求没有锚点hash时，将第一次查询第一条的数据的hash当做锚点hash
        if(StringUtils.isEmpty(pageVO.getTxHash())){
            pageVO.setTxHash(firstTxHash);
        }
        return ResponseData.ok(pageVO);
    }

    /**
     * 判断地址列表中是否存在用户地址
     * @param addressList 需要判断的地址列表
     * @param userAddressList  用户名下所有的地址
     * @return ResponseData
     */
    public boolean isHave(List<String> addressList , List<TransactionAddressVO> userAddressList){
        boolean isHave = false;//默认不包含
        for(String address : addressList){
            for(TransactionAddressVO vo : userAddressList){
                String userAddress = vo.getAddress().toLowerCase();
                address = address.toLowerCase();
                if(address.equals(userAddress)){
                    isHave = true;
                }
            }
        }
        return isHave;
    }

    /**
     * 公链信息转换方法
     * @param json 公链信息
     * @return ResponseData
     */
    public TransactionChainVO getChainVO(JSONObject json){
        TransactionChainVO vo = new TransactionChainVO();
        Integer blockHeight = json.get("blockHeight") == null? null : Integer.valueOf(json.get("blockHeight").toString());
        Long blockTime = json.get("blockTime") == null ? null :Long.valueOf(json.get("blockTime").toString());
        String txHash = json.get("txHash") == null ? "":json.get("txHash").toString();
        String contract = json.get("contract") == null ? "":json.get("contract").toString();
        String coinType = json.get("coinType") == null ? "":json.get("coinType").toString();
        String diff = json.get("diff") == null ? null :new BigDecimal(json.get("diff").toString()).toPlainString();
        String usedFee = json.get("usedFee") == null ? null :new BigDecimal(json.get("usedFee").toString()).toPlainString();
//        BigDecimal diffs = diff == null ? null :new BigDecimal(diff);
        List<String> formAddr = ToolUtils.objectCastList(json.get("fromAddr"), String.class);
        List<String> toAddr = ToolUtils.objectCastList(json.get("toAddr"), String.class);
        Integer status = json.get("status") == null? null : Integer.valueOf(json.get("status").toString());

        vo.setBlockHeight(blockHeight);
        vo.setBlockTime(blockTime);
        vo.setTxHash(txHash);
        vo.setDiff(diff);
        vo.setTransactionFee(usedFee);
        vo.setContractAddress(contract);
        vo.setCoinType(coinType);
        vo.setFromAddr(formAddr);
        vo.setToAddr(toAddr);
        vo.setTransactionStatus(status);
        return vo;
    }


    /**
     * 获取地址下交易记录公用方法
     * @return ResponseData
     */
    public JSONArray getData(String address,String net){
        JSONArray data = new JSONArray();
        String url = "";
        try{
            url = txListUrl+address+"&net="+net;
            Object result = restTemplate.getForObject(url,Object.class);
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(result));
            data = JSONArray.parseArray(JSONObject.toJSONString(json.get("data")));
        } catch (Exception e) {
            log.error("获取地址下交易记录请求执行失败URL="+url);
            return data;
        }
        return data;
    }


    /**
     * 获取地址下交易记录公用方法 - 交易记录入库优化后
     * @return ResponseData
     */
    public List<TransactionChainVO>  getAddressTransaction(List<TransactionAddressVO> userAddressList){
        List<TransactionChainVO> list = new ArrayList<>();

        for(TransactionAddressVO vo: userAddressList){
            List<TransactionChainVO> addressDataList = new ArrayList<>();
            String address = vo.getAddress();
            String symbol = vo.getSymbol();
            String net = vo.getNet();
            //查询参数中的合约地址
            String contractAddress = vo.getContractAddress();
            String tableName = "transaction_" + net + "_info";
            tableName = tableName.toLowerCase();
            int isHaveTable = quotationMapper.isHaveTable(tableName);
            if (isHaveTable > 0) {
                AddressParameterVO parameter = new AddressParameterVO();
                parameter.setAddress(address);
                parameter.setNet(net);
                parameter.setTableName(tableName);
                parameter.setSymbol(symbol);
                //获取对应链下所有符合条件的交易信息
                List<TransactionBtcInfoVO> infoList = transactionBtcInfoMapper.geNetAddressTransaction(parameter);
                for (TransactionBtcInfoVO infoVO : infoList) {
                    TransactionChainVO chainVO = new TransactionChainVO();
                    List<String> fromAddr = new ArrayList<>();
                    List<String> toAddr = new ArrayList<>();
                    if (StringUtils.isNotEmpty(infoVO.getFromAddr())) {
                        JSONArray jsonArray = JSONArray.parseArray(infoVO.getFromAddr());
                        jsonArray.stream().forEach(x->{
                            String addr =x.toString();
                            fromAddr.add(addr);
                        });
                    }
                    if (StringUtils.isNotEmpty(infoVO.getToAddr())) {
                        JSONArray jsonArray = JSONArray.parseArray(infoVO.getToAddr());
                        jsonArray.stream().forEach(x->{
                            String addr =x.toString();
                            toAddr.add(addr);
                        });
                    }
                    String diff = infoVO.getDiff() == null ? null :new BigDecimal(infoVO.getDiff().toString()).stripTrailingZeros().toPlainString();
                    String transactionFee = infoVO.getTransactionFee() == null ? null :new BigDecimal(infoVO.getTransactionFee().toString()).toPlainString();
                    String coinType = infoVO.getCoinType() == null ? "":infoVO.getCoinType().toString();
                    BeanUtils.copyProperties(infoVO, chainVO);
                    chainVO.setDiff(diff);
                    chainVO.setTransactionFee(transactionFee);
                    chainVO.setCoinType(coinType);
                    chainVO.setFromAddr(fromAddr);
                    chainVO.setToAddr(toAddr);
                    addressDataList.add(chainVO);
                }
            }
            //情况1:如果合约地址为空，表明是主币，拿取该地址下面coinType为主币类型的交易记录
            //情况2:如果合约地址不为空，表明是代币，筛选该地址下交易记录中合约地址等于查询参数中的合约地址的交易记录
//            if(StringUtils.isNotEmpty(contractAddress)){//情况一
//                for(TransactionChainVO chainVO : addressDataList){
//                    //交易记录中的合约地址
//                    String chainContractAddress = chainVO.getContractAddress();
//                    if(contractAddress.equals(chainContractAddress)){
//                        list.add(chainVO);
//                    }
//                }
//            }else{//情况二
//                for(TransactionChainVO chainVO : addressDataList){
//                    String coinType = chainVO.getCoinType();
//                    if("1".equals(coinType)){//主币交易记录
//                        list.add(chainVO);
//                    }
//                }
//            }

            for(TransactionChainVO chainVO : addressDataList){

                    list.add(chainVO);

            }

        }

        return list;
    }



    /**
     * 获取地址下余额公用方法
     * @return 
     */
    public Object requestAddressBalance(String address,String net){
       Object result = new Object();
        String url = "";
        try{
            url = balanceURL+address+"&net="+net;
            result = restTemplate.getForObject(url,Object.class);
        } catch (Exception e) {
            log.error("获取地址下余额请求执行失败URL="+url);
            return result;
        }
        return result;
    }


    /**
     * 获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param address
     * @param net
     * @return
     */
    public ResponseData getMongoAddressBalance(String address,String net){
        AddressBalanceVO vo = new AddressBalanceVO();
        List<BalanceTokensVO> tokenList = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where("address").is(address));
        TRX trx = this.mongoTemplate.findOne(query, TRX.class,net);
        if(trx != null){
            BigDecimal primaryBalance = trx.getBalance();
            List<Map<String,Object>> tokens = trx.getTokens();
            if(CollectionUtils.isNotEmpty(tokens)){
                for(Map<String , Object> map : tokens){
                    BalanceTokensVO token = new BalanceTokensVO();
//               Object contract = map.get("contract");
//               Object symbol = map.get("symbol");
//               Object decimal = map.get("decimal");
//               Object balance = map.get("balance");
//               Object coinType = map.get("coinType");
                    String symbol = String.valueOf(map.get("symbol")) == null ? "": String.valueOf(map.get("symbol"));
                    String addressName = String.valueOf(map.get("address")) == null ? "": String.valueOf(map.get("address"));
//                    Integer decimal = (Integer) map.get("decimal");
                    Integer decimal = map.get("decimal") == null ? 0 : Integer.valueOf(map.get("decimal").toString());

                    BigDecimal balance = String.valueOf(map.get("balance")) == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(map.get("balance")));
                    String balanceString = this.bigDecimalConvert(balance);
                    Integer coinType = (Integer) map.get("coinType");
                    token.setSymbol(symbol);
                    token.setAddress(addressName);
                    token.setNet(net);
                    token.setBalance(balanceString);
                    token.setDecimal(decimal);
                    token.setCoinType(coinType);
                    tokenList.add(token);
                }
            }
            String primaryBalanceString = bigDecimalConvert(primaryBalance);
            vo.setBalance(primaryBalanceString);
            vo.setTokens(tokenList);
        }
        return ResponseData.ok(vo);
    }



    /**
     * 获取用户地址下所有地址对应余额信息以及所有币种统计信息-总价值统计
     * @param addressList
     * @return
     */
    public ResponseData getBalanceStatistics(List<AddressParameterVO> addressList){
        BalanceStatisticsVO statisticsVO = new BalanceStatisticsVO();
        List<RecoverBalanceVO> balanceList = new ArrayList<>();
        List<UserBalanceVO> userList = new ArrayList<>();
        //对输入地址进行net分组
        Map<String, List<AddressParameterVO>>  addressMap = addressList.stream().collect(
                Collectors.groupingBy(AddressParameterVO::getNet));
        Map<String,BigDecimal> priceMap = getPriceMap();
        //从MongoDB里面拿出所有对余额信息
        addressMap.forEach((key,value)->{
            Map<String,BigDecimal> currencyNumberMap = new HashMap<>();
            List<String> list = new ArrayList<>();
            List<AddressParameterVO> mapList = value;
            for(AddressParameterVO vo : mapList){
                list.add(vo.getAddress());
            }
            Query query = new Query();
            query.addCriteria(Criteria.where("address").in(list));
            //拿到一条公链下所有对应余额信息
            List<TRX> trxList = this.mongoTemplate.find(query, TRX.class,key);
            //处理数据获取所有地址对应余额信息
            if(CollectionUtils.isNotEmpty(trxList)){
                for(TRX trx :trxList){
                    RecoverBalanceVO vo = new RecoverBalanceVO();
                    List<BalanceTokensVO> tokenList = new ArrayList<>();
                    String address = trx.getAddress();
                    String primarySymbol= trx.getSymbol();
                    String net = trx.getNet();
                    BigDecimal primaryBalance = trx.getBalance();
                    String primaryBalanceString = this.bigDecimalConvert(primaryBalance);
                    List<Map<String,Object>> tokens = trx.getTokens();
                    //如果map中已存在相同symbol，则相加，否则新增
                    if(currencyNumberMap.containsKey(primarySymbol)){
                        BigDecimal bal = currencyNumberMap.get(primarySymbol);
                        bal = bal.add(primaryBalance);
                        currencyNumberMap.put(primarySymbol,bal);
                    }else{
                        currencyNumberMap.put(primarySymbol,primaryBalance);
                    }

                    if(CollectionUtils.isNotEmpty(tokens)){
                        for(Map<String , Object> map : tokens){
                            BalanceTokensVO token = new BalanceTokensVO();
                            String symbol = String.valueOf(map.get("symbol")) == null ? "": String.valueOf(map.get("symbol"));
                            String addressName = String.valueOf(map.get("address")) == null ? "": String.valueOf(map.get("address"));
//                            Integer decimal = (Integer) map.get("decimal");
                            Integer decimal = map.get("decimal") == null ? 0 : Integer.valueOf(map.get("decimal").toString());
                            BigDecimal balance = String.valueOf(map.get("balance")) == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(map.get("balance")));
                            String balanceString = this.bigDecimalConvert(balance);
                            Integer coinType = (Integer) map.get("coinType");
                            token.setSymbol(symbol);
                            token.setAddress(addressName);
                            token.setNet(net);
                            token.setBalance(balanceString);
                            token.setDecimal(decimal);
                            token.setCoinType(coinType);
                            tokenList.add(token);
                            //如果map中已存在相同symbol，则相加，否则新增
                            if(currencyNumberMap.containsKey(symbol)){
                                BigDecimal bal = currencyNumberMap.get(symbol);
                                bal = bal.add(balance);
                                currencyNumberMap.put(symbol,bal);
                            }else{
                                currencyNumberMap.put(symbol,balance);
                            }
                        }
                    }
                    vo.setNet(net);
                    vo.setSymbol(net);
                    vo.setBalance(primaryBalanceString);
                    vo.setAddress(address);
                    vo.setToken(tokenList);
                    balanceList.add(vo);
                }
            }
            //拿到一条公链下所有币种统计信息
            currencyNumberMap.forEach((symbol,bal)->{
                UserBalanceVO vo = new UserBalanceVO();
                BigDecimal currencyPrice = priceMap.get(symbol);
                BigDecimal valuePrice = new BigDecimal(0);
                if(currencyPrice != null){
                    valuePrice = currencyPrice.multiply(bal);
                }
                vo.setSymbol(symbol);
                String balString = bigDecimalConvert(bal);
                vo.setCurrencyNumber(balString);
                String valuePriceString = bigDecimalConvert(valuePrice);
                vo.setValue(valuePriceString);
                String currencyPriceString =bigDecimalConvert(currencyPrice);
                vo.setCurrencyPrice(currencyPriceString);
                vo.setNet(key);
                vo.setStatisticStatus(1);
                userList.add(vo);
            });
        });
        BigDecimal totalValue = new BigDecimal(0);
        for(UserBalanceVO vo : userList){
            if(vo.getValue() != null){
                BigDecimal value = new BigDecimal(vo.getValue());
                totalValue = totalValue.add(value);
            }
        }
        UserBalanceVO totalVO = new UserBalanceVO();
        String totalValueString = bigDecimalConvert(totalValue);
        totalVO.setValue(totalValueString);
        totalVO.setStatisticStatus(2);
        userList.add(totalVO);

        statisticsVO.setBalanceList(balanceList);
        statisticsVO.setUserList(userList);
        return ResponseData.ok(statisticsVO);
    }

    /**
     * 处理Bigdecimal数据，解决科学计数法，以及末尾多余零的问题  - 公共方法
     * @param parameter 需要处理的Bigdecimal参数
     * @return
     */
    public String bigDecimalConvert(Object parameter){
        String decimal = "0";
        if(parameter != null){
            decimal = parameter == null ? "0":new BigDecimal(parameter.toString()).stripTrailingZeros().toPlainString();
        }
        return decimal;
    }



    /**
     * 获取所有币种对应价格
     * @return
     */
    public Map<String,BigDecimal> getPriceMap(){
        Map<String,BigDecimal> priceMap = new HashMap<>();
        List<CurrencyPriceEntity> list = currencyPriceMapper.getCurrencyPriceList(null);
        for(CurrencyPriceEntity entity : list){
            String currencySymbol = entity.getCurrencySymbol();
            BigDecimal priceUsd = entity.getPriceUsd();
            priceMap.put(currencySymbol,priceUsd);
        }
        return priceMap;
    }


    /**
     * 修改存储在内存中的当前块的最大高度  - 六条公链
     * @param map 需要修改的数据
     * @return
     */
    public ResponseData updateBlockHeight(Map<String,Integer> map){
        if(map != null){
            ThreadMapCache.updateHighest(map);
        }
        return ResponseData.ok("区块高度修改成功！");

    }



    /**
     * 根据公链以及币种 获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param address
     * @param net
     * @return
     */
    public ResponseData getBalanceBySymbol(String address,String net,String symbol){
        JSONObject json = new JSONObject();
        AddressBalanceVO vo = new AddressBalanceVO();
        List<BalanceTokensVO> tokenList = new ArrayList<>();
        Query query = new Query();
        BigDecimal balance = new BigDecimal(0);
        query.addCriteria(Criteria.where("address").is(address));
        TRX trx = this.mongoTemplate.findOne(query, TRX.class,net);
        if(trx != null){
            String currencyNet = trx.getNet();
            String currencySymbol = trx.getSymbol();
            if(net.equals(currencyNet)&&symbol.equals(currencySymbol)){
                balance = trx.getBalance();
            }
            List<Map<String,Object>> tokens = trx.getTokens();
            if(CollectionUtils.isNotEmpty(tokens)){
                for(Map<String , Object> map : tokens){
                    String tokenSymbol = String.valueOf(map.get("symbol")) == null ? "": String.valueOf(map.get("symbol"));
                    BigDecimal tokenBalance = String.valueOf(map.get("balance")) == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(map.get("balance")));
                    if(symbol.equals(tokenSymbol)){
                        balance = tokenBalance;
                    }
                }
            }
        }
        String balanceString = this.bigDecimalConvert(balance);
        json.put("balance",balanceString);
        return ResponseData.ok(json);
    }





    }