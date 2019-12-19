package com.bytehonor.sdk.boot.elasticsearch.core;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean: ElasticsearchRestClientFactory
 * 
 * @author lijianqiang
 *
 */
public class ElasticsearchRestClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchRestClientFactory.class);

    public static int CONNECT_TIMEOUT_MILLIS = 1000;
    public static int SOCKET_TIMEOUT_MILLIS = 30000;
    public static int CONNECTION_REQUEST_TIMEOUT_MILLIS = 500;
    public static int MAX_CONN_PER_ROUTE = 10;
    public static int MAX_CONN_TOTAL = 30;

    private static HttpHost[] HTTP_HOST;
    private RestClientBuilder builder;
    private RestClient restClient;
    private RestHighLevelClient restHighLevelClient;

    /**
     * 延迟加载(线程安全)
     *
     */
    private static class LazyHolder {
        private static ElasticsearchRestClientFactory instance = new ElasticsearchRestClientFactory();
    }

    private ElasticsearchRestClientFactory() {
        LOG.info("construct");
    }

    public static ElasticsearchRestClientFactory build(HttpHost[] httpHosts, Integer maxConnectNum,
            Integer maxConnectPerRoute) {
        HTTP_HOST = httpHosts;
        MAX_CONN_TOTAL = maxConnectNum;
        MAX_CONN_PER_ROUTE = maxConnectPerRoute;
        return LazyHolder.instance;
    }

    public static ElasticsearchRestClientFactory build(HttpHost[] httpHosts, Integer connectTimeOut, Integer socketTimeOut,
            Integer connectionRequestTime, Integer maxConnectNum, Integer maxConnectPerRoute) {
        HTTP_HOST = httpHosts;
        CONNECT_TIMEOUT_MILLIS = connectTimeOut;
        SOCKET_TIMEOUT_MILLIS = socketTimeOut;
        CONNECTION_REQUEST_TIMEOUT_MILLIS = connectionRequestTime;
        MAX_CONN_TOTAL = maxConnectNum;
        MAX_CONN_PER_ROUTE = maxConnectPerRoute;
        return LazyHolder.instance;
    }

    public void init() {
        builder = RestClient.builder(HTTP_HOST);
        setConnectTimeOutConfig();
        setMutiConnectConfig();
        restClient = builder.build();
        restHighLevelClient = new RestHighLevelClient(builder);
        LOG.info("init factory");
    }

    // 配置连接时间延时
    public void setConnectTimeOutConfig() {
        builder.setRequestConfigCallback(new RequestConfigCallback() {

            @Override
            public Builder customizeRequestConfig(Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
                requestConfigBuilder.setSocketTimeout(SOCKET_TIMEOUT_MILLIS);
                requestConfigBuilder.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS);
                return requestConfigBuilder;
            }
        });
    }

    // 使用异步httpclient时设置并发连接数
    public void setMutiConnectConfig() {
        builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {

            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(MAX_CONN_TOTAL);
                httpClientBuilder.setMaxConnPerRoute(MAX_CONN_PER_ROUTE);
                return httpClientBuilder;
            }
        });
    }

    public RestClient getClient() {
        return restClient;
    }

    public RestHighLevelClient getRhlClient() {
        return restHighLevelClient;
    }

    public void close() {
        if (restClient != null) {
            try {
                restClient.close();
            } catch (IOException e) {
                LOG.error("close error", e);
            }
        }
        LOG.info("close client");
    }

}
