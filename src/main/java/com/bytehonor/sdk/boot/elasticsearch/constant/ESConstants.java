package com.bytehonor.sdk.boot.elasticsearch.constant;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ESConstants {
	
	public static Header[] HEADERS = new Header[] {
			new BasicHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE) };
	
	public static final String TYPE_NAME = "_doc";
	
}
