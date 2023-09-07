package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.PrimaryAddressEntity;
import com.block.chain.utils.ResponseData;

import java.util.List;

/**
 * 地址主表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-21 13:30:44
 */
public interface PrimaryAddressService extends IService<PrimaryAddressEntity> {

    /**
     * 用户地址录入
     * @param list 地址信息
     * @return
     */
    public ResponseData savePrimaryAddress(List<PrimaryAddressEntity> list);

    /**
     * 统计
     * @return
     */
    public ResponseData getStatementInfo();

    /**
     * 获取公链下所有的地址信息
     * @param net
     * @return
     */
    public ResponseData getAllPrimaryAddress(String net);


    public ResponseData addAddressToRedis() throws Exception;

    public ResponseData getAddressToRedis(String net,String address) throws Exception;

    public ResponseData putAddressToRedis(PrimaryAddressEntity entity) throws Exception;




}

