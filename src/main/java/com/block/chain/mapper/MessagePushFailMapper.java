package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.MessagePushFailEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 推送消息失败信息存储表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-06-09 14:32:27
 */
@Mapper
public interface MessagePushFailMapper extends BaseMapper<MessagePushFailEntity> {

    /**
     * 获取所有推送失败的消息体
     * @return
     */
    List<MessagePushFailEntity> getALLFailMessage();
	
}
