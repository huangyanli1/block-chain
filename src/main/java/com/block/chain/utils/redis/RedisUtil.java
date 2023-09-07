package com.block.chain.utils.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Description redis工具类
 * @author: michael
 * @create: 2022-10-31 11:06
 * @Version 1.0
 */
@Lazy
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    //=============================common============================

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }/* finally {
            RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
        }*/
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    @SuppressWarnings("unchecked")
    public long getExpire(String key) throws Exception {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    @SuppressWarnings("unchecked")
    public Boolean setExpire(String key, long time) throws Exception {
        return redisTemplate.expire(key,time,TimeUnit.SECONDS);
    }
    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    @SuppressWarnings("unchecked")
    public boolean hasKey(String key) throws Exception {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }/* finally {
            RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
        }*/
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) throws Exception {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Boolean del(String key) throws Exception {
        boolean status = false;
        if (key != null && key.length() > 0) {
            if (key.length() == 1) {
                status = redisTemplate.delete(key);
            } else {
                status = redisTemplate.delete(key);
            }
        }
        return status;
    }
    

    /**
     * 根据Keys集合批量删除对应缓存
     * @param keys
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Boolean delete(Collection<String> keys) throws Exception {
        boolean status = true;
        if (keys != null && keys.size() > 0) {
        	Long result = redisTemplate.delete(keys);
        }
        return status;
    }
    //============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    @SuppressWarnings("static-access")
    public Object get(String key, int indexdb) throws Exception {
        //redisTemplate.indexdb.set(indexdb);
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 普通缓存获取符合条件的key集合
     *
     * @param keys 键
     * @return 值
     */
    @SuppressWarnings("static-access")
    public Collection<String> getKeys(String keys) throws Exception {
       
        return keys == null ? null : redisTemplate.keys(keys);
    }
    
    /**
     * 普通缓存获取
     *
     * @param keys 键
     * @return 值
     */
    @SuppressWarnings("static-access")
    public Collection<String> multiGet(Collection<String>  keys) throws Exception {
       
    	return  keys == null ? null : redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    @SuppressWarnings({"unchecked", "static-access"})
    public boolean setIndexdb(String key, Object value, int indexdb) throws Exception {
        try {
            //redisTemplate.indexdb.set(indexdb);
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    public boolean setMinutes(String key, Object value, int time) throws Exception {
        try {
            //redisTemplate.indexdb.set(indexdb);
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    @SuppressWarnings("unchecked")
    public boolean set(String key, Object value, long time) throws Exception {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    @SuppressWarnings("unchecked")
    public long incr(String key, long delta) throws Exception {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递增
     *
     * @param key     键
     * @param delta   要增加几(大于0)
     * @param indexdb 数据存储所在的库
     * @return
     */
    @SuppressWarnings({"unchecked", "static-access"})
    public long incr(String key, long delta, int indexdb) throws Exception {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }

        //redisTemplate.indexdb.set(indexdb);
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    @SuppressWarnings("unchecked")
    public long decr(String key, long delta) throws Exception {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    //================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public Object hget(String key, String item) throws Exception {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    @SuppressWarnings("unchecked")
    public Map<Object, Object> hmget(String key) throws Exception {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    @SuppressWarnings("unchecked")
    public Object hmget(String key,String secondKey) throws Exception {
        return redisTemplate.opsForHash().get(key, secondKey);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    @SuppressWarnings("unchecked")
    public boolean hmset(String key, Map<String, Object> map) throws Exception {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    @SuppressWarnings("unchecked")
    public boolean hmset(String key, Map<String, Object> map, long time) throws Exception {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    @SuppressWarnings("unchecked")
    public boolean hset(String key, String item, Object value) throws Exception {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    @SuppressWarnings("unchecked")
    public boolean hset(String key, String item, Object value, long time) throws Exception {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    @SuppressWarnings("unchecked")
    public void hdel(String key, Object... item) throws Exception {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    @SuppressWarnings("unchecked")
    public boolean hHasKey(String key, String item) throws Exception {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    @SuppressWarnings("unchecked")
    public double hincr(String key, String item, double by) throws Exception {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    @SuppressWarnings("unchecked")
    public double hdecr(String key, String item, double by) throws Exception {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    //============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    @SuppressWarnings("unchecked")
    public Set<Object> sGet(String key) throws Exception {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    @SuppressWarnings("unchecked")
    public boolean sHasKey(String key, Object value) throws Exception {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return true 存在 false不存在
     */
    @SuppressWarnings("unchecked")
    public boolean sHasKey(String key) throws Exception {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    @SuppressWarnings("unchecked")
    public long sSet(String key, Object... values) throws Exception {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    @SuppressWarnings("unchecked")
    public long sSetAndTime(String key, long time, Object... values) throws Exception {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) expire(key, time);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    @SuppressWarnings("unchecked")
    public long sGetSetSize(String key) throws Exception {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    @SuppressWarnings("unchecked")
    public long setRemove(String key, Object... values) throws Exception {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    //===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束  0 到 -1代表所有值
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束  0 到 -1代表所有值
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<String> lGets(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    @SuppressWarnings("unchecked")
    public long lGetListSize(String key) throws Exception {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引  index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setList(String key, List<String> listToken, long time) {
        try {
             redisTemplate.opsForList().leftPushAll(key, listToken);
            if (time > 0) expire(key, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据key查询string类型
     *
     * @param key
     * @return
     */
    public String getString(String key) {
        String value = null;
        try {
            value = (String) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }/* finally {
            RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
        }*/
        return value;
    }


    public  Set<Object> hmGetKeys(String key) {
        try {
            return redisTemplate.opsForHash().keys(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public  List<Object> hmGetValues(String key) {
        try {
            return redisTemplate.opsForHash().values(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
