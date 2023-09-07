package com.block.chain.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.SymbolSlugInfoEntity;
import com.block.chain.utils.ResponseData;

import java.util.List;
import java.util.Map;

/**
 * symbol-slug对应信息表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-09 14:16:09
 */
public interface SymbolSlugInfoService extends IService<SymbolSlugInfoEntity> {



    /**
     * symbol  - slug 信息录入接口
     * @param list
     * @return
     */
    public ResponseData saveSymbolSlugInfo(List<JSONObject> list);

}

