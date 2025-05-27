package com.cao.thumbsup.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * TopK接口
 */
public interface TopK {
    /**
     * 添加元素并更新TopK结构
     * @param key 元素的键值
     * @param increment 增量值
     * @return 返回添加操作的结果
     */
    AddResult add(String key, int increment);

    /**
     * 返回当前TopK元素的列表
     * @return 包含当前TopK元素的列表
     */
    List<Item> list();

    /**
     * 获取被挤出TopK的元素的队列
     * @return 被挤出元素的阻塞队列
     */
    BlockingQueue<Item> expelled();

    /**
     * 对所有计数进行衰减处理
     */
    void fading();


    /**
     * 计算所有元素的总量
     * @return 总量的长整型数值
     */
    long total();
}