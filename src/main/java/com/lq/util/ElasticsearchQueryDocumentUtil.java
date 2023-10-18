package com.lq.util;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lq.common.exception.BusinessException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.util
 * @className ElasticsearchQueryDocumentUtil
 * @description: 查询文档
 * @author: liqiang
 * @create: 2023-10-13 11:26
 **/
@Component
public class ElasticsearchQueryDocumentUtil {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    /**
     * 查询封装，返回集合
     *
     * @param searchSourceBuilder
     * @param s
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> List<T> search(SearchSourceBuilder searchSourceBuilder, Class<T> s) throws Exception {
        Document declaredAnnotation = s.getDeclaredAnnotation(Document.class);
        if (declaredAnnotation == null) {
            throw new BusinessException(String.format("class name: %s can not find Annotation [Document], please check", s.getName()));
        }
        String indexName = declaredAnnotation.indexName();
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            JSONArray jsonArray = new JSONArray();
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                T bean = JSON.parseObject(hit.getSourceAsString(), s);
                jsonArray.add(bean);
            }
            return jsonArray.toJavaList(s);
        }
        // 封装分页
        return Collections.emptyList();
    }


    /**
     * @param searchSourceBuilder
     * @param beanClass
     * @param highFields          需要高亮展示的字段
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> searchHighlightData(SearchSourceBuilder searchSourceBuilder, Class<T> beanClass, String[] highFields) throws Exception {
        Document declaredAnnotation = beanClass.getDeclaredAnnotation(Document.class);
        if (declaredAnnotation == null) {
            throw new BusinessException(String.format("class name: %s can not find Annotation [Document], please check", beanClass.getName()));
        }
        String indexName = declaredAnnotation.indexName();
        SearchRequest searchRequest = new SearchRequest(indexName);
        //生成高亮查询器
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮查询字段
        if (ArrayUtils.isNotEmpty(highFields)) {
            for (String field : highFields) {
                highlightBuilder.field(field);
            }
        }
        //如果要多个字段高亮,这项要为false
        highlightBuilder.requireFieldMatch(false);
        //高亮设置
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");

        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        //最大高亮分片数
        highlightBuilder.fragmentSize(800000);
        //从第一个分片获取高亮片段
        highlightBuilder.numOfFragments(0);
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        if (RestStatus.OK.equals(searchResponse.status())) {
            // 解析对象
            return setSearchResponse(searchResponse, beanClass);
        }
        return Collections.emptyList();
    }


    /**
     * 查询并分页
     *
     * @param index          索引名称
     * @param builder        查询条件
     * @param size           文档大小限制
     * @param from           从第几页开始
     * @param fields         需要显示的字段，逗号分隔（缺省为全部字段）
     * @param sortField      排序字段
     * @param highlightField 高亮字段
     * @return
     */
    public <T> List<T> searchListData(Class<T> beanClass,
                                      String index,
                                      SearchSourceBuilder builder,
                                      Integer size,
                                      Integer from,
                                      String fields,
                                      String sortField,
                                      String[] highlightField) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        SearchRequest request = new SearchRequest(index);
        if (StringUtils.isNotBlank(fields)) {
            //只查询特定字段。如果需要查询所有字段则不设置该项。
            builder.fetchSource(new FetchSourceContext(true, fields.split(","), Strings.EMPTY_ARRAY));
        }
        from = from <= 0 ? 0 : from * size;
        //设置确定结果要从哪个索引开始搜索的from选项，默认为0
        builder.from(from);
        builder.size(size);

        if (StringUtils.isNotBlank(sortField)) {
            //排序字段，注意如果 proposal_no 是 text类型会默认带有keyword性质，需要拼接.keyword
            builder.sort(sortField, SortOrder.ASC);
        }
        //高亮
        HighlightBuilder highlight = new HighlightBuilder();
        if (ArrayUtils.isNotEmpty(highlightField)) {
            for (String field : highlightField) {
                highlight.field(field);
            }
        }
        //关闭多个高亮
        highlight.requireFieldMatch(false);
        highlight.preTags("<span style='color:red'>");
        highlight.postTags("</span>");
        builder.highlighter(highlight);
        //不返回源数据。只有条数之类的数据。
        //builder.fetchSource(false);
        request.source(builder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        if (RestStatus.OK.equals(response.status())) {
            // 解析对象
            return setSearchResponse(response, beanClass);
        }
        return Collections.emptyList();
    }


    /**
     * 高亮结果集 特殊处理
     *
     * @param searchResponse
     * @param beanClass
     */
    public <T> List<T> setSearchResponse(SearchResponse searchResponse, Class<T> beanClass) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // 根据状态和数据条数验证是否返回了数据
        if (RestStatus.OK.equals(searchResponse.status()) && searchResponse.getHits().getTotalHits().value > 0) {
            SearchHits hits = searchResponse.getHits();
            JSONArray jsonArray = new JSONArray();
            for (SearchHit hit : hits) {
                // 将 JSON 转换成对象
                T bean = JSON.parseObject(hit.getSourceAsString(), beanClass);
                //打印高亮结果
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (MapUtil.isNotEmpty(highlightFields)) {
                    for (Map.Entry<String, HighlightField> next : highlightFields.entrySet()) {
                        HighlightField highlightField = next.getValue();
                        String highlightFieldName = next.getKey();

                        // 替换掉原来的数据
                        Text[] fragments = highlightField.getFragments();
                        if (fragments != null && fragments.length > 0) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Text fragment : fragments) {
                                stringBuilder.append(fragment);
                            }
                            // 获取method对象，其中包含方法名称和参数列表
                            highlightFieldName = highlightFieldName.substring(0, 1).toUpperCase() + highlightFieldName.substring(1);
                            Method setName = beanClass.getMethod("set" + highlightFieldName, String.class);
                            // 执行method，bean为实例对象，后面是方法参数列表；setName 没有返回值
                            setName.invoke(bean, stringBuilder.toString());
                        }
                    }
                }
                jsonArray.add(bean);
            }
            return jsonArray.toJavaList(beanClass);
        }
        return Collections.emptyList();
    }
}
