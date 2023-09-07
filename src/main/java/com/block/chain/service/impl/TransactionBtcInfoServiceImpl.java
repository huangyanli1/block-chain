package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyChainInfoEntity;
import com.block.chain.entity.TransactionBtcInfoEntity;
import com.block.chain.mapper.CurrencyChainInfoMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.mapper.TransactionBtcInfoMapper;
import com.block.chain.service.TransactionBtcInfoService;
import com.block.chain.utils.CommonService;
import com.block.chain.utils.PageUtils;
import com.block.chain.utils.ResponseData;
import com.block.chain.utils.ThreadMapCache;
import com.block.chain.vo.AddressParameterVO;
import com.block.chain.vo.TransactionBtcInfoVO;
import com.block.chain.vo.TransactionHashInfoVO;
import com.block.chain.vo.TransactionInfoVO;
import com.block.chain.vo.management.ReceiveTransactionVO;
import com.block.chain.vo.management.SendingTransactionVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Slf4j
@Service("transactionBtcInfoService")
public class TransactionBtcInfoServiceImpl extends ServiceImpl<TransactionBtcInfoMapper, TransactionBtcInfoEntity> implements TransactionBtcInfoService {

    @Autowired
    public QuotationMapper quotationMapper;

    @Autowired
    public TransactionBtcInfoMapper transactionBtcInfoMapper;

    @Autowired
    public CurrencyChainInfoMapper currencyChainInfoMapper;

    @Autowired
    public CommonService commonService;

    /**
     * 动态录入交易记录信息
     * @param list 交易记录
     * @return
     */
    @Async
    public ResponseData saveTransactionInfo(List<TransactionInfoVO> list){
        List<TransactionBtcInfoVO> resultList = new ArrayList<>();
        for(TransactionInfoVO vo : list){
            try {
            TransactionBtcInfoVO infoVO = new TransactionBtcInfoVO();
            String net = vo.getNet();
            if(StringUtils.isNotEmpty(net)){
                net = net.toLowerCase();
            }
            String tableName = "transaction_" + net + "_info";
            int isHaveTable = quotationMapper.isHaveTable(tableName);
            if(isHaveTable <= 0){
                return ResponseData.fail(tableName+"录入表不存在！");
            }
            AddressParameterVO parameter = new AddressParameterVO();
            parameter.setTableName(tableName);
            parameter.setNet(vo.getNet());
            parameter.setTxHash(vo.getTxHash());
            List<TransactionBtcInfoVO> infolist = transactionBtcInfoMapper.getListByHash(parameter);
            //如果已经存在该记录，则不保存
            if(CollectionUtils.isEmpty(infolist)){
                String from = vo.getFromAddr() == null ? "" : JSONObject.toJSONString(vo.getFromAddr());
                String to = vo.getToAddr() == null ? "" :JSONObject.toJSONString(vo.getToAddr());
                BeanUtils.copyProperties(vo, infoVO);
                infoVO.setFromAddr(from);
                infoVO.setToAddr(to);
                infoVO.setTableName(tableName);
                int size = transactionBtcInfoMapper.saveTransactionInfo(infoVO);
                if(size > 0){
                    resultList.add(infoVO);
                }
            }
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
        return ResponseData.ok(resultList);
    }


    /**
     * 动态修改交易记录
     * @param list
     * @return
     */
    public ResponseData updateTransactionInfo(List<TransactionInfoVO> list){
        for(TransactionInfoVO vo : list){
            try {
            TransactionBtcInfoVO infoVO = new TransactionBtcInfoVO();
            String net = vo.getNet();
            if(StringUtils.isNotEmpty(net)){
                net = net.toLowerCase();
            }
            String tableName = "transaction_" + net + "_info";
            int isHaveTable = quotationMapper.isHaveTable(tableName);
            if(isHaveTable <= 0){
                return ResponseData.fail(tableName+"表不存在！");
            }
            String from = vo.getFromAddr() == null ? "" : JSONObject.toJSONString(vo.getFromAddr());
            String to = vo.getToAddr() == null ? "" :JSONObject.toJSONString(vo.getToAddr());
            BeanUtils.copyProperties(vo, infoVO);
            infoVO.setFromAddr(from);
            infoVO.setToAddr(to);
            infoVO.setTableName(tableName);
            transactionBtcInfoMapper.updateTransactionInfo(infoVO);
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
        return ResponseData.ok("交易记录修改成功");
    }


    /**
     * 根据txHash和net动态修改交易记录
     * @param list
     * @return
     */
    public ResponseData updateTransactionInfoByTxHash(List<TransactionInfoVO> list){
        for(TransactionInfoVO vo : list){
            try {
                TransactionBtcInfoVO infoVO = new TransactionBtcInfoVO();
                String net = vo.getNet();
                if(StringUtils.isNotEmpty(net)){
                    net = net.toLowerCase();
                }
                String tableName = "transaction_" + net + "_info";
                int isHaveTable = quotationMapper.isHaveTable(tableName);
                if(isHaveTable <= 0){
                    return ResponseData.fail(tableName+"表不存在！");
                }
                String from = vo.getFromAddr() == null ? "" : JSONObject.toJSONString(vo.getFromAddr());
                String to = vo.getToAddr() == null ? "" :JSONObject.toJSONString(vo.getToAddr());
                BeanUtils.copyProperties(vo, infoVO);
                infoVO.setFromAddr(from);
                infoVO.setToAddr(to);
                infoVO.setTableName(tableName);
                transactionBtcInfoMapper.updateTransactionInfoByTxHash(infoVO);
            }catch (Exception e){
                e.printStackTrace();
                continue;
            }
        }
        return ResponseData.ok("交易记录修改成功");
    }

    /**
     * 获取所有状态为pending的交易记录
     * @return
     */
    public ResponseData getPendinInfoList(){
        List<TransactionBtcInfoVO> resultList = new ArrayList<>();
        List<CurrencyChainInfoEntity> list = currencyChainInfoMapper.getAllChainInfo();
        if(CollectionUtils.isEmpty(list)){
            return ResponseData.fail("");
        }
        for(CurrencyChainInfoEntity entity : list){
            String net = entity.getNet();
            if(StringUtils.isNotEmpty(net)){
                net = net.toLowerCase();
            }
            String tableName = "transaction_" + net + "_info";
            int isHaveTable = quotationMapper.isHaveTable(tableName);
            if(isHaveTable > 0){
                TransactionBtcInfoVO vo = new TransactionBtcInfoVO();
                vo.setTableName(tableName);
                List<TransactionBtcInfoVO> pendingList = transactionBtcInfoMapper.getPendingTransactionList(vo);
                resultList.addAll(pendingList);
            }
        }
        return ResponseData.ok(resultList);

    }


    /**
     * 后台管理系统-加密货币交易管理 - 接收币列表
     * @return
     */
    public ResponseData getReceiveTransactionList(List<String> addressList,String txHash,Integer isConfim,Long startTime,Long endTime,Integer page, Integer pageSize){
        List<TransactionBtcInfoVO> list = new ArrayList<>();
        List<ReceiveTransactionVO> receiveList = new ArrayList<>();
        if(CollectionUtils.isEmpty(addressList)){
            addressList = null;
        }
        Integer listCount = 0;
        list = transactionBtcInfoMapper.getReceiveList(addressList,txHash,isConfim,startTime,endTime,(page-1)*pageSize,pageSize);
        listCount = transactionBtcInfoMapper.getReceiveCount(addressList,txHash,isConfim,startTime,endTime,(page-1)*pageSize,pageSize);
        if(CollectionUtils.isNotEmpty(list)){
            for(TransactionBtcInfoVO vo : list){
                ReceiveTransactionVO receive = new ReceiveTransactionVO();
                BeanUtils.copyProperties(vo, receive);
                Integer  transactionStatus = vo.getTransactionStatus();
                String  net = vo.getNet();
                //如果交易区块高度为null，则广播(确认块数)为null，否则则用公链最高区块高度 -  交易区块高度 = 广播（确认块数）

                if(vo.getBlockHeight() == null){
                    receive.setConfirmInfo(null);//确认信息 - 广播块的数量
                }else{
                    Integer maxHighest = ThreadMapCache.getHighestMap(net) == null ? 0 : ThreadMapCache.getHighestMap(net);
                    if(maxHighest > vo.getBlockHeight()){
                        receive.setConfirmInfo(maxHighest - vo.getBlockHeight());//确认信息 - 广播块的数量
                    }
                }
                //后续需添加
//                receive.setConfirmInfo(null);//确认信息 - 广播块的数量
//                receive.setConfirmTime(null);//添加时间 - 确认块时间
                if(vo.getBlockTime() != null){
                    String block = String.valueOf(vo.getBlockTime());
                    Boolean isMillise = commonService.isMilliseSecond(block);
                    if(isMillise){
                        Long blcokTime = vo.getBlockTime();
                        receive.setBlockTime(blcokTime/1000);
                    }
                }

                //完成 3 4 到账 4
                Integer isComplete = 0;//是否完成
                Integer isConfirm = 0;//是否到账
                if(transactionStatus == 3 || transactionStatus == 4){
                    isComplete = 1 ;
                }else{
                    isComplete = 0;
                }
                if(transactionStatus == 4){
                    isConfirm = 1 ;
                }else{
                    isConfirm = 0;
                }
                receive.setIsComplete(isComplete);
                receive.setIsConfirm(isConfirm);
                receiveList.add(receive);
            }
        }

        PageUtils pageUtils = new PageUtils(receiveList,listCount,pageSize,page);
        return ResponseData.ok(pageUtils);
    }


    /**
     * 后台管理系统-加密货币交易管理 - 发送币列表
     * @return
     */
    public ResponseData getSendingTransactionList(List<String> addressList,String symbol,Integer minDiff,Integer maxDiff,Long startTime,Long endTime,Integer page, Integer pageSize){
        List<SendingTransactionVO> receiveList = new ArrayList<>();
        if(CollectionUtils.isEmpty(addressList)){
            addressList = null;
        }
        List<TransactionBtcInfoVO> list = transactionBtcInfoMapper.getSendingList(addressList,symbol,minDiff,maxDiff,startTime,endTime,(page-1)*pageSize,pageSize);
        Integer listCount = transactionBtcInfoMapper.getSendingCount(addressList,symbol,minDiff,maxDiff,startTime,endTime,(page-1)*pageSize,pageSize);

        if(CollectionUtils.isNotEmpty(list)){
            for(TransactionBtcInfoVO vo : list){
                SendingTransactionVO receive = new SendingTransactionVO();
                BeanUtils.copyProperties(vo, receive);
                if(vo.getBlockTime() != null){
                    String block = String.valueOf(vo.getBlockTime());
                    Boolean isMillise = commonService.isMilliseSecond(block);
                    if(isMillise){
                        Long blcokTime = vo.getBlockTime();
                        receive.setBlockTime(blcokTime/1000);
                    }
                }
                BigDecimal diff = vo.getDiff() == null ? new BigDecimal(0) :vo.getDiff().negate();
                BigDecimal transactionFee = vo.getTransactionFee() == null ? new BigDecimal(0) :vo.getTransactionFee();
                BigDecimal actualAmount = diff.add(transactionFee);
                receive.setDiff(diff);
                receive.setActualAmount(actualAmount);//实际金额
                receive.setSendErrorNumber(0);//发送错误次数 ，写死0
                receiveList.add(receive);
            }
        }

        PageUtils pageUtils = new PageUtils(receiveList,listCount,pageSize,page);
        return ResponseData.ok(pageUtils);
    }



    /**
     * 根据交易hash和公链net 获取接收和发送详情
     * @return
     */
    public ResponseData getInfoByTxHash(String net ,String txHash){
        TransactionBtcInfoVO vo = new TransactionBtcInfoVO();
        TransactionHashInfoVO hashVo = new TransactionHashInfoVO();
        String nets = net;
        if(StringUtils.isNotEmpty(net)){
            net = net.toLowerCase();
        }
        String tableName = "transaction_" + net + "_info";
        int isHaveTable = quotationMapper.isHaveTable(tableName);
        if(isHaveTable > 0){
            AddressParameterVO parameter = new AddressParameterVO();
            parameter.setTableName(tableName);
            parameter.setNet(nets);
            parameter.setTxHash(txHash);
            List<TransactionBtcInfoVO> list = transactionBtcInfoMapper.getListByHash(parameter);
            if(CollectionUtils.isNotEmpty(list)){
                vo = list.get(0);
                List<String> fromAddr = new ArrayList<>();
                List<String> toAddr = new ArrayList<>();
                BeanUtils.copyProperties(vo, hashVo);
                if (StringUtils.isNotEmpty(vo.getFromAddr())) {
                    JSONArray jsonArray = JSONArray.parseArray(vo.getFromAddr());
                    jsonArray.stream().forEach(x->{
                        String addr =x.toString();
                        fromAddr.add(addr);
                    });
                }
                if (StringUtils.isNotEmpty(vo.getToAddr())) {
                    JSONArray jsonArray = JSONArray.parseArray(vo.getToAddr());
                    jsonArray.stream().forEach(x->{
                        String addr =x.toString();
                        toAddr.add(addr);
                    });
                }
                hashVo.setFromAddr(fromAddr);
                hashVo.setToAddr(toAddr);
            }
        }
        return ResponseData.ok(hashVo);
    }





}