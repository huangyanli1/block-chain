package com.block.chain.service.impl;


import com.block.chain.service.ChainInfoAnalysisService;

import com.block.chain.utils.ResponseData;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service("chainInfoAnalysisServiceImpl")
public class ChainInfoAnalysisServiceImpl implements ChainInfoAnalysisService {


    /**
     * 解析合约输入参数
     * @param inputData 合约输入参数
     * @return
     */
    public ResponseData getTypeList(String inputData){
        List<TypeReference<?>> temp = Arrays.asList(
                new TypeReference<DynamicArray<Address>>() {
                },
                new TypeReference<Address>() {
                },
                new TypeReference<DynamicArray<Uint256>>() {
                }
        );
        List<Type> ret = decodeInputData(inputData, temp);

        return ResponseData.ok(ret);
    }


    /**
     * decode input data
     *
     * @param inputData
     * @param outputParameters
     * @return
     */
    public static List<Type> decodeInputData(String inputData, List<TypeReference<?>> outputParameters) {
        List<Type> result = FunctionReturnDecoder.decode(
                inputData.substring(10),
                convert(outputParameters)
        );
        return result;
    }

    /**
     * ? to Type
     *
     * @param input
     * @return
     */
    public static List<TypeReference<Type>> convert(List<TypeReference<?>> input) {
        List<TypeReference<Type>> result = new ArrayList<>(input.size());
        result.addAll(
                input.stream()
                        .map(typeReference -> (TypeReference<Type>) typeReference)
                        .collect(Collectors.toList()));
        return result;
    }
}