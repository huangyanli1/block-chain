package com.block.chain.controller;



import com.block.chain.service.*;
import com.block.chain.utils.PageResult;
import com.block.chain.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * Analysis configuration controller
 * @author michael
 * @date 2022/11/08
 */

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private ChainInfoAnalysisService chainInfoAnalysisService;


    /**
     *  解析合约输入参数
     * @param inputdata 合约输入参数
     * @return
     */
    @GetMapping("getTypeListByInputData")
    public ResponseData<PageResult> getTypeListByInputData(@RequestParam String inputdata){
        return chainInfoAnalysisService.getTypeList(inputdata);
    }

}
