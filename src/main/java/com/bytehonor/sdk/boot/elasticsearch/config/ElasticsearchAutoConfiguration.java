package com.bytehonor.sdk.boot.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
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

    @Autowired
    private ElasticsearchProperties elasticsearchRestProperties;

    @Bean
    @ConditionalOnMissingBean(HttpHost.class)
    @ConditionalOnProperty(prefix = "elasticsearch.boot", name = "rest-host")
    public HttpHost httpHost() {
        return new HttpHost(elasticsearchRestProperties.getRestHost(), elasticsearchRestProperties.getRestPort(),
                elasticsearchRestProperties.getRestSchema());
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
