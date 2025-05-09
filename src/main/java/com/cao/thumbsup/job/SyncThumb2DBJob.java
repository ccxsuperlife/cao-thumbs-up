package com.cao.thumbsup.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.thumbsup.mapper.BlogMapper;
import com.cao.thumbsup.model.entity.Thumb;
import com.cao.thumbsup.model.enums.ThumbTypeEnum;
import com.cao.thumbsup.service.ThumbService;
import com.cao.thumbsup.util.RedisKeyUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 小曹同学
 * @date 2025/5/9
 * @description 定时将 Redis 中的临时点赞数据同步到数据库中
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SyncThumb2DBJob {

    private final ThumbService thumbService;

    private final BlogMapper blogMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    // 每 10 秒执行一次
    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("开始执行定时任务：{}", DateUtil.now());
        DateTime nowDate = DateUtil.date();
        // 秒数 0~9,返回上一分钟的50s
        int second = (DateUtil.second(nowDate) / 10 - 1) * 10;
        if (second == -10) {
            second = 50;

            // 获取上一分钟
            nowDate = DateUtil.offsetMinute(nowDate, -1);
        }
        String timeSlice = DateUtil.format(nowDate, "HH:mm:") + second;
        syncThumb2DBByDate(timeSlice);
        log.info("当前时间片：{},临时数据同步完成", timeSlice);
    }

    private void syncThumb2DBByDate(String timeSlice) {
        // 获取临时点赞和取消点赞数据
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean thumbMapEmpty = CollUtil.isEmpty(allTempThumbMap);
        if (thumbMapEmpty) {
            return;
        }
        // 同步 点赞 到数据库
        // 构建插入列表并收集blogId
        Map<Long, Long> blogThumbCountMap = new HashMap<>();
        ArrayList<Thumb> thumbList = new ArrayList<>();
        LambdaQueryWrapper<Thumb> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        boolean needRemove = false;
        for (Object userIdBlogIdObj : allTempThumbMap.keySet()) {
            String userIdBlogId = (String) userIdBlogIdObj;
            //按照 : 分割字符串内容
            String[] userIdAndBlogId = userIdBlogId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndBlogId[0]);
            Long blogId = Long.valueOf(userIdAndBlogId[1]);
            // -1 取消点赞 1 点赞
            Integer thumbType = Integer.valueOf(allTempThumbMap.get(userIdBlogId).toString());
            if (thumbType == ThumbTypeEnum.INCR.getValue()) {
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setBlogId(blogId);
                // 批量更新
                thumbList.add(thumb);
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()) {
                needRemove = true;
                // 拼接查询条件,批量删除
                lambdaQueryWrapper.or().eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId);
            } else {
                if (thumbType != ThumbTypeEnum.NON.getValue()) {
                    log.error("点赞类型错误：{}", userId + "," + blogId + "," + thumbType);
                }
                continue;
            }
            // 计算点赞增量
            blogThumbCountMap.put(blogId, blogThumbCountMap.getOrDefault(blogId, 0L) + thumbType);
        }
        // 批量插入点赞记录
        if (CollUtil.isNotEmpty(thumbList)) {
            thumbService.saveBatch(thumbList);
        }
        // 批量删除点赞记录
        if (needRemove) {
            thumbService.remove(lambdaQueryWrapper);
        }

        // 批量更新博客点赞数
        blogMapper.batchUpdateThumbCount(blogThumbCountMap);

        // 异步删除临时点赞记录
        redisTemplate.delete(tempThumbKey);


    }


}
