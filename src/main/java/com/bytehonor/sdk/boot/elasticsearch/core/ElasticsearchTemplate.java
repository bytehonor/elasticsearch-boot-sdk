package com.bytehonor.sdk.boot.elasticsearch.core;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import com.bytehonor.sdk.boot.elasticsearch.util.ElasticsearchUtils;
import com.google.gson.Gson;

/**
 * Bean
 * 
 * @author lijianqiang
 *
 */
public class ElasticsearchTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchTemplate.class);

    private static final Gson GSON = new Gson();

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 查询索引是否存在
     * 
     * @param rawIndexName
     * @return
     * @throws IOException
     */
    public boolean existsIndex(String rawIndexName) throws IOException {
        String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);
        GetIndexRequest request = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 创建索引
     * 
     * @param rawIndexName
     * @return
     * @throws IOException
     */
    public CreateIndexResponse createIndex(String rawIndexName) throws IOException {
        String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName); // 创建索引
        XContentBuilder builder = ElasticsearchUtils.initBuilderWhenCreateIndex();
        createIndexRequest.source(builder);
        return restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    public AcknowledgedResponse putMapping(String indexName, XContentBuilder builder) throws IOException {
        indexName = ElasticsearchUtils.formatIndexName(indexName, applicationName);
        PutMappingRequest request = new PutMappingRequest(indexName);
//        request.type(ESConstants.TYPE_NAME);
        request.source(builder);
//        request.timeout(TimeValue.timeValueMinutes(2));
        return restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
    }

    public void putMappingAsync(String rawindexName, XContentBuilder builder, @Nullable ESWriteListener callback) {
        final String indexName = ElasticsearchUtils.formatIndexName(rawindexName, applicationName);
        final PutMappingRequest request = new PutMappingRequest(indexName);
//        request.type(ESConstants.TYPE_NAME);
        request.source(builder);
        ActionListener<AcknowledgedResponse> listener = new ActionListener<AcknowledgedResponse>() {

            @Override
            public void onFailure(Exception e) {
                LOG.error("putMappingAsync error, indexName:{}, error:{}", indexName, e);
                if (callback != null) {
                    callback.onFinished(new ESWriteResult(false, e.getMessage(), "indexAsync"));
                }
            }

            @Override
            public void onResponse(AcknowledgedResponse response) {
                LOG.info("putMappingAsync, indexName:{}, isAcknowledged:{}", indexName, response.isAcknowledged());
                if (callback != null) {
                    callback.onFinished(new ESWriteResult("indexAsync"));
                }
            }
        };
        restHighLevelClient.indices().putMappingAsync(request, RequestOptions.DEFAULT, listener);
    }

    /**
     * 索引文档
     * 
     * @param rawIndexName
     * @param model
     * @return
     * @throws IOException
     */
    public <T extends ESData> IndexResponse index(String rawIndexName, T model) throws IOException {
        Assert.notNull(model, "model cannt be null!");
        Assert.notNull(model.esid(), "esid cannt be null!");
        String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);
        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.id(model.esid());
        String source = GSON.toJson(model);
        indexRequest.source(source, XContentType.JSON);
        return restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 索引文档，异步
     * 
     * @param rawIndexName
     * @param model
     * @param listener
     */
    public <T extends ESData> void indexAsync(String rawIndexName, T model, @Nullable ESWriteListener callback) {
        Assert.notNull(model, "model cannt be null!");
        Assert.notNull(model.esid(), "esid cannt be null!");
        final String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);
        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.id(model.esid());
        String source = GSON.toJson(model);
        indexRequest.source(source, XContentType.JSON);
        ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {

            @Override
            public void onResponse(IndexResponse response) {
                LOG.debug("indexAsync indexName:{}, id:{}", response.getIndex(), response.getId());
                if (callback != null) {
                    callback.onFinished(new ESWriteResult("indexAsync"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                LOG.error("indexAsync error, indexName:{}, error:{}", indexName, e);
                if (callback != null) {
                    callback.onFinished(new ESWriteResult(false, e.getMessage(), "indexAsync"));
                }
            }

        };
        restHighLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, listener);
    }

    /**
     * 删除文档
     * 
     * @param rawIndexName
     * @param esid
     * @return
     * @throws IOException
     */
    public DeleteResponse delete(String rawIndexName, String esid) throws IOException {
        Assert.notNull(esid, "esid cannt be null!");
        String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);
        DeleteRequest request = new DeleteRequest(indexName, esid);
        return restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除文档，异步
     * 
     * @param rawIndexName
     * @param esid
     * @param listener
     */
    public void deleteAsync(String rawIndexName, String esid, @Nullable ESWriteListener callback) {
        Assert.notNull(esid, "esid cannt be null!");
        final String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);
        DeleteRequest request = new DeleteRequest(indexName, esid);
        ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {

            @Override
            public void onResponse(DeleteResponse response) {
                LOG.info("deleteAsync indexName:{}, id:{}", response.getIndex(), response.getId());
                if (callback != null) {
                    callback.onFinished(new ESWriteResult("deleteAsync"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                LOG.error("deleteAsync error, indexName:{}, error:{}", indexName, e.getMessage());
                if (callback != null) {
                    callback.onFinished(new ESWriteResult(false, e.getMessage(), "deleteAsync"));
                }
            }
        };
        restHighLevelClient.deleteAsync(request, RequestOptions.DEFAULT, listener);
    }

    /**
     * 批量索引操作
     * 
     * @param rawIndexName
     * @param models
     * @return
     * @throws IOException
     */
    public <T extends ESData> BulkResponse bulk(String rawIndexName, List<T> models) throws IOException {
        String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);

        BulkRequest bulkRequest = new BulkRequest();

        for (T model : models) {
            if (model.esid() == null) {
                continue;
            }
            IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.id(model.esid());
            String source = GSON.toJson(model);
            indexRequest.source(source, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 批量索引操作，异步
     * 
     * @param rawIndexName
     * @param models
     * @param listener
     */
    public <T extends ESData> void bulkAsync(String rawIndexName, List<T> models, @Nullable ESWriteListener callback) {
        final String indexName = ElasticsearchUtils.formatIndexName(rawIndexName, applicationName);

        BulkRequest bulkRequest = new BulkRequest();

        for (T model : models) {
            if (model.esid() == null) {
                continue;
            }
            IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.id(model.esid());
            String source = GSON.toJson(model);
            indexRequest.source(source, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {

            @Override
            public void onResponse(BulkResponse response) {
                for (BulkItemResponse bulkItemResponse : response.getItems()) {
                    DocWriteResponse itemResponse = bulkItemResponse.getResponse();

                    if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                            || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                        IndexResponse indexResponse = (IndexResponse) itemResponse;
                        LOG.debug("IndexResponse, index:{}, id:{}, version:{}", indexResponse.getIndex(), indexResponse.getId(),
                                indexResponse.getVersion());
                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                        LOG.debug("UpdateResponse, index:{}, id:{}, version:{}", updateResponse.getIndex(),
                                updateResponse.getId(), updateResponse.getVersion());
                    } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                        LOG.info("DeleteResponse, index:{}, id:{}, version:{}", deleteResponse.getIndex(),
                                deleteResponse.getId(), deleteResponse.getVersion());
                    }
                }
                if (callback != null) {
                    callback.onFinished(new ESWriteResult("bulkAsync"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                LOG.error("bulkAsync error, indexName:{}, error:{}", indexName, e.getMessage());
                if (callback != null) {
                    callback.onFinished(new ESWriteResult(false, e.getMessage(), "bulkAsync"));
                }
            }

        };
        restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
    }

}
