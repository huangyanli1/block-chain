package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.mapper.CurrencyChainInfoMapper;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.mapper.SymbolSlugInfoMapper;
import com.block.chain.service.CurrencyTypeService;
import com.block.chain.utils.*;
import com.block.chain.vo.CurrencyTypeVO;
import com.block.chain.vo.management.CurrencyPriceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 *
 * Currency Configuration Service Business Layer Processing
 * @author michael
 * @date 2022-10-10
 */
@Slf4j
@Service("currencyTypeService")
public class CurrencyTypeServiceImpl extends ServiceImpl<CurrencyTypeMapper, CurrencyTypeEntity> implements CurrencyTypeService {

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    public QuotationMapper quotationMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private SymbolSlugInfoMapper symbolSlugInfoMapper;

    @Autowired
    private CurrencyChainInfoMapper currencyChainInfoMapper;


    public ResponseData getCurrencyTypeList(Long id ,Integer isBuy,Integer isExchange ,Integer currencyStatus,String net, String symbol, Integer coinType, Integer page, Integer pageSize){
        List<CurrencyTypeEntity> list = currencyTypeMapper.getCurrencyListByParamer(id,isBuy,isExchange,net,symbol,currencyStatus,coinType,(page-1)*pageSize,pageSize);
        Integer listCount = currencyTypeMapper.getCurrencyListByParamerCount(id,isBuy,isExchange,net,symbol,currencyStatus,coinType,(page-1)*pageSize,pageSize);
        PageUtils pageUtils = new PageUtils(list,listCount,pageSize,page);
        return ResponseData.ok(pageUtils);
    }

    public ResponseData<CurrencyTypeEntity> getCurrencyTypeById(String id){
        CurrencyTypeEntity entity = currencyTypeMapper.selectById(id);
        return ResponseData.ok(entity);
    }

    public ResponseData<Long>  saveCurrencyType(CurrencyTypeVO vo){
        CurrencyTypeEntity entity = new CurrencyTypeEntity();
        BeanUtils.copyProperties(vo, entity);
        JSONArray platforms = vo.getPlatforms();
        if(platforms != null){
            String  platform = platforms.toJSONString();
            entity.setPlatforms(platform);
        }
        entity.setCurrencyStatus(2);//默认已下线状态
        if(entity != null ){
            String symbol = entity.getSymbol();
            String fullName = entity.getFullName();
            String logoUrl = entity.getLogoUrl();
            String details = entity.getDetails();
            Integer coinType = entity.getCoinType();
            if(StringUtils.isEmpty(symbol)||StringUtils.isEmpty(fullName)||StringUtils.isEmpty(logoUrl)||StringUtils.isEmpty(details)||coinType == null){
                return ResponseData.fail("必填参数为空！");
            }
        }
        entity.setIsDelete(0);
        entity.setCreateDate(new Date());
        this.automaticCreateTable(entity.getSymbol());
        int size = currencyTypeMapper.insert(entity);
        if(size <= 0){
            return ResponseData.fail("币种配置保存失败！");
        }
        return ResponseData.ok(entity.getId());
    }

    public boolean automaticCreateTable(String symbol){
        boolean isCreate = false;//默认未创建
        Map<String, Object> columnMap = new HashMap<String , Object>();
        columnMap.put("symbol", symbol);
        List<CurrencyTypeEntity> list = this.listByMap(columnMap);
            String symbols = symbol.toLowerCase();
            String tableName = "quotation_"+symbols+"_usd";
            int isHaveTable = quotationMapper.isHaveTable(tableName);
            if(isHaveTable <= 0){
                int createTable  = quotationMapper.automaticCreateTable(tableName);
                if(createTable > 0){
                    isCreate = true;
                }
            }
        return isCreate;
    }


    public ResponseData  deleteCurrencyType(List<String> list){
        if(CollectionUtils.isEmpty(list)){
            return ResponseData.fail("修改参数不能为空！");
        }
        for(String id : list){
            CurrencyTypeEntity entity = new CurrencyTypeEntity();
            entity.setId(Long.valueOf(id));
            entity.setIsDelete(1);
            currencyTypeMapper.updateById(entity);
        }
        return ResponseData.ok("删除成功");
    }


    /**
     * 币种配置 - 币种配置修改
     * @param vo 配置修改参数
     * @return
     */
    public ResponseData  updateCurrencyType( CurrencyTypeVO vo){
        CurrencyTypeEntity entity = new CurrencyTypeEntity();
        BeanUtils.copyProperties(vo, entity);
        JSONArray platforms = vo.getPlatforms();
        String  platform = platforms == null ? "" : platforms.toJSONString();
        entity.setPlatforms(platform);
        entity.setUpdateDate(new Date());

        int size = 0;
        if (entity.getId() != null){
            size = currencyTypeMapper.updateById(entity);
        }
        if(size <= 0){
            return ResponseData.fail("币种配置信息修改失败！");
        }
        return ResponseData.ok("币种配置修改成功");
    }


    /**
     * 币种配置 - 修改币种上线状态
     * @param vo 配置修改参数(id和币种状态)
     * @return
     */
    public ResponseData  updateCurrencyStatus(CurrencyTypeVO vo){
        CurrencyTypeEntity entity = new CurrencyTypeEntity();
        BeanUtils.copyProperties(vo, entity);
        entity.setUpdateDate(new Date());
        int size = 0;
        if (entity.getId() != null){
            size = currencyTypeMapper.updateById(entity);
        }
        if(size <= 0){
            return ResponseData.fail("币种状态修改失败！");
        }
        return ResponseData.ok("币种状态修改成功");
    }


    /**
     * 后台管理系统 - 币种管理列表 - 币种列表查询
     * @param page 当前页
     * @param pageSize 每页展示数量
     * @return
     */
    public ResponseData<PageResult> getCurrencyPriceList(Integer page, Integer pageSize){
        List<CurrencyPriceVO> list = new ArrayList<>();
        Map<String, BigDecimal> priceMap = commonService.getPriceMap();
        List<CurrencyTypeEntity> currenyList = currencyTypeMapper.getCurrencyList();
        if(CollectionUtils.isNotEmpty(currenyList)){
            for(CurrencyTypeEntity entity : currenyList){
                String symbol = entity.getSymbol();
                CurrencyPriceVO vo = new CurrencyPriceVO();
                BeanUtils.copyProperties(entity, vo);
                BigDecimal price = priceMap.get(symbol) == null ? new BigDecimal(0) : priceMap.get(symbol);
                vo.setSymbolPrice(price.stripTrailingZeros().toPlainString());
                list.add(vo);
            }
        }
        Integer total = list .size();
        PageResult pageResult = new PageResult();
        pageResult.setData(list);
        pageResult.setCount(Long.valueOf(total));
        pageResult.setCurrentPage(Long.valueOf(page));
        pageResult.setPageSize(Long.valueOf(pageSize));

        return ResponseData.ok(pageResult);
    }

    /**
     * 获取所有symbol数据 - 对symbol去重
     * @return
     */
    public ResponseData  getSymbolList(){
        List<CurrencyTypeEntity> list = new ArrayList<>();
        List<CurrencyTypeEntity> currenyList = currencyTypeMapper.getCurrencyList();
        if(CollectionUtils.isNotEmpty(currenyList)){
            //获取所有的币种信息，并按照币种符号去重
            list = currenyList.stream()
                    .filter(distinctByKeys(CurrencyTypeEntity::getSymbol))
                    .collect(Collectors.toList());

        }

        return ResponseData.ok(list);

    }

    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();

        return t ->
        {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());

            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }

    public ResponseData  getExchangeList(){
        List<CurrencyTypeEntity> list =currencyTypeMapper.getExchangeList();
        return ResponseData.ok(list);
    }

    public ResponseData  getAllExchangeList(){
        List<JSONObject> JsonList = new ArrayList<>();
        List<CurrencyTypeEntity> list =currencyTypeMapper.getAllExchangeList();
        if(CollectionUtils.isNotEmpty(list)){
            for(CurrencyTypeEntity entity : list){
                JSONObject json = new JSONObject();
                String net = entity.getNet();
                String symbol = entity.getSymbol();
                String logoUrl = entity.getLogoUrl();
                json.put("net",net);
                json.put("symbol",symbol);
                json.put("logoUrl",logoUrl);
                JsonList.add(json);
            }
        }
        return ResponseData.ok(JsonList);
    }






}