package com.bytehonor.sdk.boot.elasticsearch.core;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.bytehonor.sdk.boot.elasticsearch.demo.Foo;
import com.bytehonor.sdk.boot.elasticsearch.demo.FooMapping;
import com.bytehonor.sdk.boot.elasticsearch.util.ElasticsearchUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.JVM)
public class ElasticsearchTemplateTest {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchTemplateTest.class);

    private static final String INDEX_NAME = "foo";

    @Autowired(required = false)
    private ElasticsearchTemplate elasticsearchWriteTemplate;

    @Test
    public void testConfig() {
        boolean isOk = elasticsearchWriteTemplate != null;
        assertTrue("*testConfig*", isOk);
    }

//    @Test
    public void testExistIndex() {
        boolean isOk = true;
        try {
            boolean exists = elasticsearchWriteTemplate.existsIndex(INDEX_NAME);
            if (exists == false) {
                elasticsearchWriteTemplate.createIndex(INDEX_NAME);
                elasticsearchWriteTemplate.putMapping(INDEX_NAME, FooMapping.elasticsearch());
                exists = elasticsearchWriteTemplate.existsIndex(INDEX_NAME);
            }
            LOG.info("foo is exist:{}", exists);
        } catch (IOException e) {
            LOG.error("testExistIndex", e);
            isOk = false;
        }
        assertTrue("*testExistIndex*", isOk);
    }

    // @Test
    public void testCreateIndex() {
        boolean isOk = true;
        try {
            boolean exists = elasticsearchWriteTemplate.existsIndex(INDEX_NAME);
            isOk = exists;
            if (exists == false) {
                CreateIndexResponse response = elasticsearchWriteTemplate.createIndex(INDEX_NAME);
                isOk = response.isAcknowledged();
                LOG.info("createIndex foo, isAcknowledged:{}, isShardsAcknowledged:{}", response.isAcknowledged(),
                        response.isShardsAcknowledged());
            }
        } catch (IOException e) {
            LOG.error("testCreateIndex", e);
            isOk = false;
        }
        assertTrue("*testCreateIndex*", isOk);
    }

    // @Test
    public void testPutMapping() {
        boolean isOk = true;
        try {
            XContentBuilder builder = FooMapping.elasticsearch();
            AcknowledgedResponse response = elasticsearchWriteTemplate.putMapping(INDEX_NAME, builder);
            isOk = response.isAcknowledged();
        } catch (IOException e) {
            LOG.error("testPutMapping", e);
            isOk = false;
        }
        assertTrue("*testPutMapping*", isOk);
    }

    // @Test
    public void testIndex() {
        long now = System.currentTimeMillis();
        Foo foo = new Foo();
        foo.setId(now);
        foo.setUnid("unid" + now);
        foo.setName("foo" + (now % 1000));
        foo.setCreateAt(now);
        foo.setDetail("数据类型一般文本使用text(可分词进行模糊查询)；keyword无法被分词(不需要执行分词器)，用于精确查找");

        boolean isOk = false;
        try {
            IndexResponse response = elasticsearchWriteTemplate.index(INDEX_NAME, foo);
            EsWriteResult result = ElasticsearchUtils.checkIndexResponse(response);
            LOG.info("index, success:{}, message:{}", result.isSuccess(), result.getMessage());
            isOk = result.isSuccess();
        } catch (IOException e) {
            LOG.error("testBulk", e);
            isOk = false;
        }
        assertTrue("*testIndex*", isOk);
    }

//  @Test
    public void testBulk() {
        List<Foo> foos = new ArrayList<Foo>(32);
        long now = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Foo foo = new Foo();
            foo.setId(now + i);
            foo.setUnid("testBuild" + (now + i));
            foo.setName("testBuild" + i);
            foo.setCreateAt(now + i);
            foo.setDetail("数据类型一般文本使用text(可分词进行模糊查询)；keyword无法被分词(不需要执行分词器)，用于精确查找");
            foos.add(foo);
        }
        boolean isOk = true;
        try {
            BulkResponse bulkResponse = elasticsearchWriteTemplate.bulk(INDEX_NAME, foos);
            EsWriteResult result = ElasticsearchUtils.checkBulkResponse(bulkResponse);
            LOG.info("bulk, success:{}, message:{}", result.isSuccess(), result.getMessage());
            isOk = result.isSuccess();
        } catch (IOException e) {
            LOG.error("testBulk", e);
            isOk = false;
        }
        assertTrue("*testBulk*", isOk);
    }

}
