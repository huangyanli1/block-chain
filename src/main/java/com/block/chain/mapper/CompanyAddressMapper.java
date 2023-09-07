package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.CompanyAddressEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公司地址存储表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-24 14:23:03
 */
@Mapper
public interface CompanyAddressMapper extends BaseMapper<CompanyAddressEntity> {
    /**
     * 获取全部公司地址信息
     * @return
     */
    public List<CompanyAddressEntity> getAllCompanyAddress();

    /**
     * 获取公司地址信息列表
     * @param page
     * @param pageSize
     * @return
     */
    public List<CompanyAddressEntity> getCompanyAddressList(@Param("page") Integer page, @Param("pageSize") Integer pageSize);

    public Integer getCompanyAddressCount();

}
