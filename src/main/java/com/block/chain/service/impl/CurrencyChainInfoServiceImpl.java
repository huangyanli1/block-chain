package com.block.chain.service.impl;

import com.block.chain.entity.CurrencyChainInfoEntity;
import com.block.chain.mapper.CurrencyChainInfoMapper;
import com.block.chain.service.CurrencyChainInfoService;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.CurrencyChainInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Service("currencyChainInfoService")
public class CurrencyChainInfoServiceImpl extends ServiceImpl<CurrencyChainInfoMapper, CurrencyChainInfoEntity> implements CurrencyChainInfoService {

    @Autowired
    private CurrencyChainInfoMapper currencyChainInfoMapper;


    /**
     * 获取所有的公链信息
     * @return
     */
    public ResponseData  getAllChainInfo(){

        List<CurrencyChainInfoVO> infoList = new ArrayList<>();
        List<CurrencyChainInfoEntity> list = currencyChainInfoMapper.getAllChainInfo();
        for(CurrencyChainInfoEntity entity : list){
            CurrencyChainInfoVO vo = new CurrencyChainInfoVO();
            BeanUtils.copyProperties(entity, vo);
            infoList.add(vo);
        }
        return ResponseData.ok(infoList);
    }



}