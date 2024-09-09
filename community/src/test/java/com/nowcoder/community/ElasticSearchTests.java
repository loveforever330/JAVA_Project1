package com.nowcoder.community;

import com.nowcoder.community.CommunityApplication;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import org.elasticsearch.client.asyncsearch.DeleteAsyncSearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }
    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
    }
    @Test
    public void testUpdate(){
        DiscussPost post=discussPostMapper.selectDiscussPostById(237);
        post.setContent("我是新人,我爱灌水");
        discussPostRepository.save(post);
    }
    @Test
    public void testDelete(){
        //discussPostRepository.deleteById(237);
        discussPostRepository.deleteAll();
    }
    @Test
    public void testSearchByRepository() {
        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))//分页处理
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        // 执行搜索
        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);

        // 处理结果
        List<DiscussPost> posts = new ArrayList<>();
        for (SearchHit<DiscussPost> hit : searchHits) {
            DiscussPost post = hit.getContent();

            // 获取高亮字段并设置到结果对象中
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("title")) {
                post.setTitle(highlightFields.get("title").get(0));  // 将高亮的title替换原有title
            }
            if (highlightFields.containsKey("content")) {
                post.setContent(highlightFields.get("content").get(0));  // 将高亮的content替换原有content
            }

            posts.add(post);
        }

        // 输出结果
        System.out.println("总数：" + searchHits.getTotalHits());
        posts.forEach(post -> System.out.println(post));

    }

}
