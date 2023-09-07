package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.CompanyAddressEntity;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.CompanyAddressVO;


import java.util.List;

/**
 * 公司地址存储表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-24 14:23:03
 */
public interface CompanyAddressService extends IService<CompanyAddressEntity> {

    /**
     * 公司地址录入
     * @param list 地址信息
     * @return
     */
    public ResponseData saveCompanyAddress(List<CompanyAddressEntity> list);


    /**
     * 判断地址是否为公司地址
     * @param address 地址
     * @param net 公链
     * @return
     */
    public ResponseData isCompanyAddress(String address ,String net);

    /**
     * 公司地址 - 公司钱包地址列表信息查询
     * @param list
     * @return
     */
    public ResponseData getCompanyAddressList(List<CompanyAddressVO> list);


    /**
     * 后台财务管理 - 公司钱包管理入账出账统计
     * @return
     */
    public ResponseData getQuantityStatistics(List<CompanyAddressVO> list);
}

