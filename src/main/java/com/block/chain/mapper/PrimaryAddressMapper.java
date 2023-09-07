package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.PrimaryAddressEntity;
import com.block.chain.vo.TransactionBtcInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地址主表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-21 13:30:44
 */
@Mapper
public interface PrimaryAddressMapper extends BaseMapper<PrimaryAddressEntity> {

    /**
     * 获取所有的地址信息
     * @return
     */
    List<PrimaryAddressEntity> getPrimaryaddressList();

    /**
     * 获取公链下所有的地址信息
     * @return
     */
    List<PrimaryAddressEntity> getAllPrimaryAddress(@Param("net") String net);

    int getAddressCount(@Param("net") String net);
    List<PrimaryAddressEntity> getAddressList(@Param("net") String net,@Param("page") Integer page, @Param("pageSize") Integer pageSize);

    List<PrimaryAddressEntity> getAddressListByNet(@Param("net") String net,@Param("address") String address);



}
