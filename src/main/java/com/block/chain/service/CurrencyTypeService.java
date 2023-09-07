package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.utils.PageResult;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.CurrencyTypeVO;


import java.util.List;
import java.util.Map;

/**
 * 币种配置
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-10 09:33:44
 */
public interface CurrencyTypeService extends IService<CurrencyTypeEntity> {


    /**
     * 获取币种配置信息
     * @return
     */
    public ResponseData getCurrencyTypeList(Long id ,Integer isBuy,Integer isExchange ,Integer currencyStatus,String net, String symbol, Integer coinType, Integer page, Integer pageSize);

    /**
     * 保存币种配置
     * @param vo 币种配置信息
     * @return
     */
    public ResponseData<Long> saveCurrencyType(CurrencyTypeVO vo);

    /**
     * 币种配置删除
     * @param list 需删除的币种配置Id
     * @return
     */
    public ResponseData deleteCurrencyType(List<String> list);

    /**
     * 币种配置 - 币种配置修改
     * @param vo 配置修改参数
     * @return
     */
    public ResponseData  updateCurrencyType( CurrencyTypeVO vo);

    /**
     * 获取币种配置详情信息
     * @return
     */
    public ResponseData<CurrencyTypeEntity> getCurrencyTypeById(String id);

    /**
     * 币种配置 - 修改币种上线状态
     * @param vo 配置修改参数(id和币种状态)
     * @return
     */
    public ResponseData  updateCurrencyStatus(CurrencyTypeVO vo);


    /**
     * 后台管理系统 - 币种管理列表 - 币种列表查询
     * @param page 当前页
     * @param pageSize 每页展示数量
     * @return
     */
    public ResponseData<PageResult> getCurrencyPriceList(Integer page, Integer pageSize);


    /**
     * 获取所有symbol数据 - 对symbol去重
     * @return
     */
    public ResponseData  getSymbolList();

    /**
     * 获取公链为ETH和MATIC下可兑换币种
     * @return
     */
    public ResponseData  getExchangeList();

    /**
     * 获取所有可兑换币种
     * @return
     */
    public ResponseData  getAllExchangeList();
}

