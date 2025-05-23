package com.cao.thumbsup.mapper;

import com.cao.thumbsup.model.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
* @author baogondian
* @description 针对表【blog】的数据库操作Mapper
* @createDate 2025-04-28 13:51:59
* @Entity com.cao.thumbsup.model.entity.Blog
*/
public interface BlogMapper extends BaseMapper<Blog> {

    /**
     * 批量更新博客点赞数
     * @param countMap
     * key blogId 博客ID
     * value thumbCount 点赞数
     */
    void batchUpdateThumbCount(@Param("countMap") Map<Long,Long> countMap);

}




