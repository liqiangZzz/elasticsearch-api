package com.lq.util;

import com.alibaba.fastjson.JSON;
import com.lq.common.exception.BusinessException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @program: elasticsearch-api
 * @pageName com.lq.util
 * @className ElasticsearchCRUDDocumentUtil
 * @description: spring data Elasticsearch Rest 操作文档
 * @author: liqiang
 * @create: 2023-10-13 10:43
 **/
@Component
public class ElasticsearchCRUDDocumentUtil {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public IndexResponse index(Object o) throws Exception {
        Document declaredAnnotation = o.getClass().getDeclaredAnnotation(Document.class);
        if (declaredAnnotation == null) {
            throw new BusinessException(String.format("class name: %s can not find Annotation [Document], please check", o.getClass().getName()));
        }
        String indexName = declaredAnnotation.indexName();

        // 创建索引请求对象
        IndexRequest request = new IndexRequest(indexName);

        //获取业务 Id
        Field fieldByAnnotation = getFieldByAnnotation(o, Id.class);

        if (fieldByAnnotation != null) {
            fieldByAnnotation.setAccessible(true);
            try {
                Object id = fieldByAnnotation.get(o);
                request = request.id(id.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // 将对象转为json
        String userJson = JSON.toJSONString(o);
        request.source(userJson, XContentType.JSON);
        //执行文档操作
        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }


    /**
     * 获取字段的注解
     * @param o
     * @param annotationClass
     * @return
     */
    public static Field getFieldByAnnotation(Object o, Class annotationClass) {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        if (declaredFields.length > 0) {
            for (Field f : declaredFields) {
                if (f.isAnnotationPresent(annotationClass)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * 根据 索引名称和业务Id删除问题
     *
     * @param indexName
     * @param docId
     * @return
     * @throws IOException
     */
    public boolean deleteDoc(String indexName, String docId) throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, docId);
        DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        // 解析response
        String index = deleteResponse.getIndex();
        String id = deleteResponse.getId();
        long version = deleteResponse.getVersion();
        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure :
                    shardInfo.getFailures()) {
                String reason = failure.reason();
            }
        }
        return true;
    }

    /**
     * 根据json类型更新文档
     *
     * @param indexName
     * @param docId
     * @param o
     * @return
     * @throws IOException
     */
    public boolean updateDoc(String indexName, String docId, Object o) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, docId);
        request.doc(JSON.toJSONString(o), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        String index = updateResponse.getIndex();
        String id = updateResponse.getId();
        long version = updateResponse.getVersion();
        if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            return true;
        }
        return false;
    }

    /**
     * 根据Map类型更新文档
     *
     * @param indexName
     * @param docId
     * @param map
     * @return
     * @throws IOException
     */
    public boolean updateDoc(String indexName, String docId, Map<String, Object> map) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, docId);
        request.doc(map);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        String index = updateResponse.getIndex();
        String id = updateResponse.getId();
        long version = updateResponse.getVersion();
        if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            return true;
        }
        return false;
    }

    /**
     * 批量插入文档
     * 文档存在 则插入
     * 文档不存在 则更新
     *
     * @param list
     * @param izAsync
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> boolean batchSaveOrUpdate(List<T> list, boolean izAsync) throws Exception {
        Object o1 = list.get(0);
        Document declaredAnnotation = o1.getClass().getDeclaredAnnotation(Document.class);
        if (declaredAnnotation == null) {
            throw new BusinessException(String.format("class name: %s can not find Annotation [@Document], please check", o1.getClass().getName()));
        }
        String indexName = declaredAnnotation.indexName();

        BulkRequest request = new BulkRequest(indexName);
        for (Object o : list) {
            String jsonStr = JSON.toJSONString(o);
            IndexRequest indexRequest = new IndexRequest().source(jsonStr, XContentType.JSON);

            Field fieldByAnnotation = getFieldByAnnotation(o, Id.class);

            if (fieldByAnnotation != null) {
                fieldByAnnotation.setAccessible(true);
                try {
                    Object id = fieldByAnnotation.get(o);
                    indexRequest = indexRequest.id(id.toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            request.add(indexRequest);
        }
        if (izAsync) {
            BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            return outResult(bulkResponse);
        } else {
            restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkResponse) {
                    outResult(bulkResponse);
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        }
        return true;
    }


    private boolean outResult(BulkResponse bulkResponse) {
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();
            IndexResponse indexResponse = (IndexResponse) itemResponse;
            if (bulkItemResponse.isFailed()) {
                return false;
            }
        }
        return true;
    }
}
