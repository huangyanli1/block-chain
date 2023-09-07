package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.SymbolSlugInfoEntity;
import com.block.chain.mapper.SymbolSlugInfoMapper;
import com.block.chain.service.SymbolSlugInfoService;
import com.block.chain.utils.ResponseData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.xml.ws.soap.Addressing;


@Service("symbolSlugInfoService")
public class SymbolSlugInfoServiceImpl extends ServiceImpl<SymbolSlugInfoMapper, SymbolSlugInfoEntity> implements SymbolSlugInfoService {


    @Autowired
    private SymbolSlugInfoMapper SymbolSlugInfoMapper;


    /**
     * symbol  - slug 信息录入接口
     * @param list
     * @return
     */
    public ResponseData saveSymbolSlugInfo(List<JSONObject> list) {
        List<SymbolSlugInfoEntity> infoList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
            for(JSONObject json : list){
                SymbolSlugInfoEntity entity = new SymbolSlugInfoEntity();
                String symbol = json.get("symbol") == null ? "" : json.get("symbol").toString();
                String slug = json.get("slug") == null ? "" : json.get("slug").toString();
                entity.setSymbol(symbol);
                entity.setSlug(slug);
                infoList.add(entity);
            }
        }
        this.saveBatch(infoList);
        return ResponseData.ok("录入成功");
    }



    }