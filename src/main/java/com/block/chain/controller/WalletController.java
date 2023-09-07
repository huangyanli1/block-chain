package com.block.chain.controller;


import com.block.chain.entity.WalletTransactionEntity;
import com.block.chain.service.CurrencyTypeService;
import com.block.chain.service.MongoDBService;
import com.block.chain.service.TransactionBtcInfoService;
import com.block.chain.service.WalletTransactionService;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.AddressParameterVO;
import com.block.chain.vo.TransactionAddressVO;
import com.block.chain.vo.TransactionInfoVO;
import com.block.chain.vo.TransactionParamsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Wallet configuration controller
 * @author michael
 * @date 2022/10/17
 */
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private CurrencyTypeService currencyTypeService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private TransactionBtcInfoService transactionBtcInfoService;

    @Autowired
    private MongoDBService mongoDBService;


    /**
     * 钱包交易记录保存
     * @param entity 钱包交易记录信息
     * @return
     */
    @PostMapping("/saveWalletTransaction")
    public ResponseData saveWalletTransaction(@RequestBody WalletTransactionEntity entity){
        return walletTransactionService.saveWalletTransaction(entity);
    }

    /**
     * 钱包交易记录修改
     * @param entity
     * @return
     */
    @PostMapping("/updateWalletTransaction")
    public ResponseData  updateWalletTransaction(@RequestBody  WalletTransactionEntity entity){
        return walletTransactionService.updateWalletTransaction(entity);
    }

    /**
     * 获取所有交易状态为unconfirmed的交易数据
     * @return
     */
    @GetMapping("getAllUnconfirmedList")
    public ResponseData getAllUnconfirmedList(){
        return walletTransactionService.getAllUnconfirmedList();
    }


    /**
     * 按条件筛选交易记录
     * @param addressList 交易地址列表
     * @return
     */
    @GetMapping("getTransactionList")
    public ResponseData getTransactionList(@RequestParam List<String> addressList){
        return walletTransactionService.getTransactionList(addressList);
    }

    /**
     * 获取地址下余额
     * @param address 地址
     * @param net 链
     * @return
     */
    @GetMapping("getAddressBalance")
    public ResponseData getAddressBalance(@RequestParam String address,@RequestParam String net){
        return walletTransactionService.getMongoAddressBalance(address,net);
//      return walletTransactionService.getAddressBalance(address,net);
    }


    /**
     * 批量获取获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param addressList 查询地址信息
     * @return
     */
    @PostMapping("/addressBalanceList")
    public ResponseData addressBalanceList(@RequestBody  List<TransactionAddressVO> addressList){
        return mongoDBService.getMongoAddressBalanceList(addressList);
    }

    /**
     * 账号- 按交易类型获取用户账号下所有地址满足条件的交易记录
     * @param params
     * @return
     */
    @PostMapping("/getAccountChainTransaction")
    public ResponseData getAccountChainTransaction(@RequestBody TransactionParamsVO params){
        return walletTransactionService.getAccountChainTransaction(params);
    }

    /**
     * 公链信息优化 - 公链交易记录动态录入数据库
     * @param list 交易记录
     * @return
     */
    @PostMapping("/saveTransactionInfo")
    public ResponseData saveTransactionInfo(@RequestBody List<TransactionInfoVO> list){
        return transactionBtcInfoService.saveTransactionInfo(list);
    }

    /**
     * 公链信息优化 - 公链交易记录动态修改交易记录
     * @param list
     * @return
     */
    @PostMapping("/updateTransactionInfo")
    public ResponseData updateTransactionInfo(@RequestBody List<TransactionInfoVO> list){
        return transactionBtcInfoService.updateTransactionInfo(list);
    }


    /**
     * 公链信息优化 - 根据txHash和net动态修改交易记录
     * @param list
     * @return
     */
    @PostMapping("/updateTransactionInfoByTxHash")
    public ResponseData updateTransactionInfoByTxHash(@RequestBody List<TransactionInfoVO> list){
        return transactionBtcInfoService.updateTransactionInfoByTxHash(list);
    }

    /**
     * 获取所有状态为pending的交易记录
     * @return
     */
    @GetMapping("/getPendinInfoList")
    public ResponseData getPendinInfoList(){
        return transactionBtcInfoService.getPendinInfoList();
    }

    /**
     * 获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @return
     */
    @GetMapping("/getMongoAddressBalance")
    public ResponseData getMongoAddressBalance(@RequestParam String address,@RequestParam String net){
        return walletTransactionService.getMongoAddressBalance(address,net);
    }




    /**
     * 获取用户地址下所有地址对应余额信息以及所有币种统计信息-总价值统计
     * @param addressList
     * @return
     */
    @PostMapping("/balanceStatistics")
    public ResponseData balanceStatistics(@RequestBody List<AddressParameterVO> addressList){
        return walletTransactionService.getBalanceStatistics(addressList);
    }



    /**
     * 修改存储在内存中的当前块的最大高度  - 六条公链
     * @param map 需要修改的数据
     * @return
     */
    @PostMapping("/updateBlockHeight")
    public ResponseData updateBlockHeight(@RequestBody Map<String,Integer> map){
        return walletTransactionService.updateBlockHeight(map);
    }

    /**
     * 交易 - 根据交易hash和公链net 获取接收和发送详情
     * @return
     */
    @GetMapping("/getInfoByTxHash")
    public ResponseData getInfoByTxHash(@RequestParam String net ,@RequestParam String txHash){
        return transactionBtcInfoService.getInfoByTxHash(net,txHash);
    }


    /**
     * 根据公链以及币种 获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param address
     * @param net
     * @return
     */
    @GetMapping("/getBalanceBySymbol")
    public ResponseData getBalanceBySymbol(@RequestParam String address,@RequestParam String net,@RequestParam String symbol){
        return walletTransactionService.getBalanceBySymbol(address,net,symbol);
    }

}
