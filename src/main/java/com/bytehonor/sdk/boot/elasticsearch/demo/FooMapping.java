package com.bytehonor.sdk.boot.elasticsearch.demo;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class FooMapping {

	public static XContentBuilder elasticsearch() throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			builder.startObject("properties");
			{
				builder.startObject("id");
				{
					builder.field("type", "long");
				}
				builder.endObject();

				builder.startObject("unid");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();

				builder.startObject("name");
				{
					builder.field("type", "text");
					builder.field("analyzer", "ik_max_word");
				}
				builder.endObject();

				builder.startObject("detail");
				{
					builder.field("type", "text");
					builder.startObject("fields");
					{
						builder.startObject("keyword");
						{
							builder.field("type", "keyword");
							builder.field("ignore_above", 256);
						}
						builder.endObject();
					}
					builder.endObject();
				}
				builder.endObject();

				builder.startObject("createAt");
				{
					builder.field("type", "date");
					builder.field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
				}
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

}
