package com.bytehonor.sdk.boot.elasticsearch.util;

import java.io.IOException;
import java.util.Objects;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytehonor.sdk.boot.elasticsearch.constant.ESConstants;
import com.bytehonor.sdk.boot.elasticsearch.core.ESWriteResult;

public class ElasticsearchUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchUtils.class);

    /**
     * 格式化索引名称
     * 
     * @param rawIndexName
     * @param applicationName
     * @return
     */
    public static String formatIndexName(String rawIndexName, String applicationName) {
        Objects.requireNonNull(rawIndexName, "rawIndexName");
        Objects.requireNonNull(applicationName, "applicationName");
        if (rawIndexName.startsWith(applicationName) == false) {
            rawIndexName = new StringBuilder(applicationName).append("_").append(rawIndexName).toString();
        }
        return rawIndexName;
    }

    public static XContentBuilder initBuilderWhenCreateIndex() throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject();
        {
            builder.startObject("mappings");
            {
                builder.startObject(ESConstants.TYPE_NAME).endObject();
            }
            builder.endObject();
            builder.startObject("settings");
            {
                builder.field("number_of_shards", 3).field("number_of_replicas", 1);
            }
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }

    public static ESWriteResult checkIndexResponse(IndexResponse indexResponse) {
        Objects.requireNonNull(indexResponse, "indexResponse");
        ESWriteResult result = new ESWriteResult();
        String index = indexResponse.getIndex();
        String id = indexResponse.getId();
        long version = indexResponse.getVersion();
        LOG.info("index, id:{}, index:{}, version:{}", id, index, version);
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            LOG.debug("CREATED");
            return result;
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            LOG.debug("UPDATED");
            return result;
        }
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        LOG.info("shardInfo.getTotal():{}, shardInfo.getSuccessful():{}", shardInfo.getTotal(),
                shardInfo.getSuccessful());
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            LOG.warn("shardInfo.getTotal() != shardInfo.getSuccessful()");
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                String reason = failure.reason();
                LOG.error("testIndex reason:{}", reason);
                result.setMessage(reason);
            }
        }
        return result;
    }

    public static ESWriteResult checkBulkResponse(BulkResponse bulkResponse) {
        ESWriteResult result = new ESWriteResult("bulk");
        int create = 0;
        int update = 0;
        int delete = 0;
        BulkItemResponse[] items = bulkResponse.getItems();
        if (items == null || items.length < 1) {
            result.setSuccess(false);
            result.setMessage(bulkResponse.buildFailureMessage());
            return result;
        }
        for (BulkItemResponse bulkItemResponse : items) {
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();

            if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                    || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                IndexResponse indexResponse = (IndexResponse) itemResponse;
                LOG.debug("IndexResponse, index:{}, id:{}, version:{}", indexResponse.getIndex(), indexResponse.getId(),
                        indexResponse.getVersion());
                create++;
            } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                LOG.debug("UpdateResponse, index:{}, id:{}, version:{}", updateResponse.getIndex(),
                        updateResponse.getId(), updateResponse.getVersion());
                update++;
            } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                LOG.info("DeleteResponse, index:{}, id:{}, version:{}", deleteResponse.getIndex(),
                        deleteResponse.getId(), deleteResponse.getVersion());
                delete++;
            }
        }
        StringBuilder sb = new StringBuilder().append("create:").append(create).append(", update:").append(update)
                .append(", delete:").append(delete);
        result.setMessage(sb.toString());
        return result;
    }
}
