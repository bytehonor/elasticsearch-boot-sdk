package com.bytehonor.sdk.boot.elasticsearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Elasticsearch High Level Rest Properties
 * 
 * @author lijianqiang
 *
 */
@ConfigurationProperties(prefix = "elasticsearch.boot")
public class ElasticsearchProperties {

    /**
     * Elasticsearch Rest Host
     */
    private String restHost;

    /**
     * Elasticsearch Rest Port, default: 9200
     */
    private Integer restPort = 9200;

    /**
     * Elasticsearch Rest Schema, default: http
     */
    private String restSchema = "http";

    /**
     * Elasticsearch Rest ConnectNum, default: 5
     */
    private Integer restConnectNum = 5;

    /**
     * Elasticsearch Rest ConnectPerRoute, default: 10
     */
    private Integer restConnectPerRoute = 10;

    public String getRestHost() {
        return restHost;
    }

    public void setRestHost(String restHost) {
        this.restHost = restHost;
    }

    public Integer getRestPort() {
        return restPort;
    }

    public void setRestPort(Integer restPort) {
        this.restPort = restPort;
    }

    public String getRestSchema() {
        return restSchema;
    }

    public void setRestSchema(String restSchema) {
        this.restSchema = restSchema;
    }

    public Integer getRestConnectNum() {
        return restConnectNum;
    }

    public void setRestConnectNum(Integer restConnectNum) {
        this.restConnectNum = restConnectNum;
    }

    public Integer getRestConnectPerRoute() {
        return restConnectPerRoute;
    }

    public void setRestConnectPerRoute(Integer restConnectPerRoute) {
        this.restConnectPerRoute = restConnectPerRoute;
    }

}
