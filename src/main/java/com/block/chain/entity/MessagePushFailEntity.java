package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 推送消息失败信息存储表
 * 
 * @author michael
 * @email 123456789@qq.com
 */
@Data
@TableName("message_push_fail")
public class MessagePushFailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId
	private Long id;
	/**
	 * 需要推送的消息内容
	 */
	private String data;
	/**
	 * 推送消息的分组类型
	 */
	private String messageType;
	/**
	 * 修改时间
	 */
	private Date updateDate;
	/**
	 * 创建时间
	 */
	private Date createDate;

}
