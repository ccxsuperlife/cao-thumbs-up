package com.cao.thumbsup.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * TopK接口
 */
public interface TopK {
    // 添加元素并更新TopK结构
    AddResult add(String key, int increment);

    // 返回当前TopK元素的列表
    List<Item> list();

    // 获取被挤出TopK的元素的队列
    BlockingQueue<Item> expelled();

    // 对所有计数进行衰减
    void fading();

    long total();
}