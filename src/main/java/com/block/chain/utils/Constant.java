package com.block.chain.utils;

/**
 * 常量
 *
 * @author michael
 */
public class Constant {
    /**
     * 超级管理员ID
     */
    public static final int SUPER_ADMIN = 1;
    /**
     * 当前页码
     */
    public static final String PAGE = "currPage";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "pageSize";


    //定时任务 - 交易所不拉取币种

    public static final String USDT = "USDT";

    public static final String USDX = "USDX";

    //定时任务拉取价格时，需要特殊处理的币种   ETH = FETH
    public static final String ETH = "ETH";
    public static final String FETH = "FETH";
    public static final String FETH_Name = "fETH";

    //校验公司地址时，BTC和TRX对大小写敏感，需要特殊处理

    public static final String BTC = "BTC";

    public static final String TRX = "TRX";

    //查询交易记录类型：1、全部 2、划转 3、转账 4、收款
    public static final String ONE = "1";//全部
    public static final String TWO = "2";//划转
    public static final String THREE = "3";//转账
    public static final String FOUR = "4";//收款


    //保存币种时自动建表需要排除的币种
    public static final String EXCLUDE_CURRENCY="USDT.e,USDC.e";



    /**
     * 业务线id.
     * 0: 用户在在Ulla生态外部发起的交易，即: 不是Ulla合作方或Ulla自有系统发起的交易
     * 1: Ulla 钱包
     * 2:货币加密支付
     */
    public static final Integer BUSINESS_ZERO = 0;//用户在在Ulla生态外部发起的交易，即: 不是Ulla合作方或Ulla自有系统发起的交易
    public static final Integer BUSINESS_ONE = 1;//Ulla 钱包
    public static final Integer BUSINESS_TWO = 2;//货币加密支付

    /**
     * 币币兑换交易订单录入 - 传入业务时 币币兑换标识
     */
    public static final String BUSSINESS_EXCHANGE="exchange";
    /**
     * 业务消息推送时币币兑换订单标识
     */
    public static final Integer ORDER_NO_TYPE=1;





    //定时任务 - 锁定时间
    /**
     * 最小锁定时间,一般设置成定时任务小一点
     */
    public static final int MIN_LOCK_TIME = 100;//单位毫秒
    /**
     * 最大锁定时间,一般设置成比正常执行时间长的值
     */
    public static final int MAX_LOCK_TIME = 1000 * 2;//单位毫秒


    //后台管理系统 - 首页统计 - 接收统计 - MongoDB接收集合名
    public static final String currency_receive = "currency_receive";

    //后台管理系统 - 首页统计 - 财务收款统计(公司地址) - MongoDB发送集合名
    public static final String company_receive = "company_receive";

    //后台管理系统 - 首页统计 - 发送统计 - MongoDB发送集合名
    public static final String currency_sending = "currency_sending";


    //exchange now 不支持的链
    public static final String notsupport_network = "heco";
    public static final String notsupport_network_op = "op";
    //exchange now可用币对插入Redis缓存Key
    public static final String available_pairs = "AvailablePairs";
    public static final String available_pairs_opposite_direction = "AvailablePairsOppositeDirection";//反向标识



    //exchange now数据SHA256加密key
    public static final String sha256_key = "92C730596FA4786C6A44966A59F87577";
    //exchange now跨链桥成功失败状态
    public static final String finished = "finished";
    public static final String failed = "failed";
    //exchange now 兑换订单状态交易状态0 预创建 1：订单创建且等待 waiting 4：订单取消 cancel 5:订单完成 finished 6：订单失败 failed
    public static final Integer  exchange_precreate = 0; //预创建
    public static final Integer  exchange_waiting = 1; //订单创建且等待转账状态
    public static final Integer  exchange_cancel = 4; //订单取消
    public static final Integer  exchange_finished = 5; //订单完成
    public static final Integer  exchange_failed = 6; //订单失败








    //后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 当前用户总价值数据和历史接收币总价值获取适用的页面场景类型 ： 1 发送页面  2 接收页面 3 兑换页面
    public static final String one = "1";
    public static final String two = "2";
    public static final String three = "3";



    //需要特殊处理的币种  原因：该币种主币名称是ETH 和原有以太币重名，所以需要单独处理
    public static final String ARBI_NAME ="ARBI";
    public static final String ARBI_SYMBOL = "ARB";


    //获取法币汇率所需要的主流货币
    public static final String USD = "USD";
    public static final String GBP = "GBP";
    public static final String EUR = "EUR";
    public static final String JPY = "JPY";


    //消息发送时的消息类型
    public static final String WOW_EXCHANGE = "WOW-EXCHANGE";//兑换交易时
    public static final String ASSET_BROADCAST_CRYPTO = "ASSET-BROADCAST-CRYPTO";//加密货币消息推送 价格等
    public static final String ASSET_BROADCAST_FIAT = "ASSET-BROADCAST-FIAT";//法币消息推送 价格等


    //redis地址初始化目录
    public static final String SCANBLOCK_USERADDRESS = "ScanBlock:UserAddress:";//兑换交易时



}
