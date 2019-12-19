package com.bytehonor.sdk.boot.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.bytehonor.sdk.boot.elasticsearch.core.ElasticsearchRestClientFactory;
import com.bytehonor.sdk.boot.elasticsearch.core.ElasticsearchTemplate;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchAutoConfiguration.class);

    @Autowired
    private ElasticsearchProperties elasticsearchRestProperties;

    @Bean
    @ConditionalOnMissingBean(HttpHost.class)
    @ConditionalOnProperty(prefix = "elasticsearch.boot", name = "rest-host-list")
    public HttpHost[] httpHost() {
        // 解析hostlist配置信息
        String[] split = elasticsearchRestProperties.getRestHostList().split(",");
        int length = split.length;
        // 创建HttpHost数组，其中存放es主机和端口的配置信息
        HttpHost[] httpHosts = new HttpHost[length];
        for (int i = 0; i < length; i++) {
            String item = split[i];
            String[] host = item.split(":");
            httpHosts[i] = new HttpHost(host[0], Integer.parseInt(host[1]),
                    elasticsearchRestProperties.getRestSchema());
        }
        LOG.info("init HttpHost, size:{}", length);
        return httpHosts;
    }

    /**
     * init AND close IMPORTANT
     * 
     * close
     * 
     * @return EleasticsearchRestClientFactory
     */
    @Bean(initMethod = "init", destroyMethod = "close")
    @ConditionalOnBean(HttpHost.class)
    @ConditionalOnMissingBean(ElasticsearchRestClientFactory.class)
    public ElasticsearchRestClientFactory getFactory() {
        return ElasticsearchRestClientFactory.build(httpHost(), elasticsearchRestProperties.getRestConnectNum(),
                elasticsearchRestProperties.getRestConnectPerRoute());
    }

    @Bean
    @Scope("singleton")
    @ConditionalOnBean(ElasticsearchRestClientFactory.class)
    public RestClient getRestClient() {
        return getFactory().getClient();
    }

    @Bean
    @Scope("singleton")
    @ConditionalOnBean(ElasticsearchRestClientFactory.class)
    public RestHighLevelClient getRHLClient() {
        return getFactory().getRhlClient();
    }

    @Bean
    @ConditionalOnBean(HttpHost.class)
    @ConditionalOnMissingBean(ElasticsearchTemplate.class)
    public ElasticsearchTemplate elasticsearchWriteTemplate() {
        return new ElasticsearchTemplate();
    }
}
