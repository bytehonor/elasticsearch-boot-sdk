package com.bytehonor.sdk.boot.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.bytehonor.sdk.boot.elasticsearch.core.ElasticsearchRestClientFactory;
import com.bytehonor.sdk.boot.elasticsearch.core.ElasticsearchWriteTemplate;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    @Autowired
    private ElasticsearchProperties elasticsearchRestProperties;

    @Bean
    @ConditionalOnMissingBean(HttpHost.class)
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
    @ConditionalOnMissingBean(ElasticsearchRestClientFactory.class)
    public ElasticsearchRestClientFactory getFactory() {
        return ElasticsearchRestClientFactory.build(httpHost(), elasticsearchRestProperties.getRestConnectNum(),
                elasticsearchRestProperties.getRestConnectPerRoute());
    }

    @Bean
    @Scope("singleton")
    public RestClient getRestClient() {
        return getFactory().getClient();
    }

    @Bean
    @Scope("singleton")
    public RestHighLevelClient getRHLClient() {
        return getFactory().getRhlClient();
    }

    @Bean
    @ConditionalOnMissingBean(ElasticsearchWriteTemplate.class)
    public ElasticsearchWriteTemplate elasticsearchWriteTemplate() {
        return new ElasticsearchWriteTemplate();
    }
}
