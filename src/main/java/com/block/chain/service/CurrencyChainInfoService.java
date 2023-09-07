package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.CurrencyChainInfoEntity;
import com.block.chain.utils.ResponseData;


/**
 * 公链信息表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-18 14:12:58
 */
public interface CurrencyChainInfoService extends IService<CurrencyChainInfoEntity> {

    /**
     * 获取所有的公链信息
     * @return
     */
    public ResponseData getAllChainInfo();
}

