package com.block.chain.service;


import com.block.chain.utils.ResponseData;

/**
 * 链上信息拆解分析类
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-08 18:45:18
 */
public interface ChainInfoAnalysisService{

    /**
     * 解析合约输入参数
     * @param inputData 合约输入参数
     * @return
     */
    public ResponseData getTypeList(String inputData);


}

