package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void saveDiscussPost(DiscussPost post){
        discussPostRepository.save(post);
    }
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }
    public static Logger logger= LoggerFactory.getLogger(ElasticsearchService.class);
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        logger.info("搜索关键字: {}", keyword);

        // 构建搜索条件
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        logger.info("搜索查询: {}", searchQuery.toString());
        // 使用 ElasticsearchRestTemplate 进行搜索
        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);

        // 处理搜索结果
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit<DiscussPost> hit : searchHits) {
            DiscussPost post = hit.getContent();

            // 处理高亮显示
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("title")) {
                post.setTitle(highlightFields.get("title").get(0));
            }
            if (highlightFields.containsKey("content")) {
                post.setContent(highlightFields.get("content").get(0));
            }

            list.add(post);
        }

        // 构建分页信息
        Pageable pageable = PageRequest.of(current, limit);
        long totalHits = searchHits.getTotalHits();

        // 使用 PageImpl 创建 Page 对象
        return new PageImpl<>(list, pageable, totalHits);
    }


}
