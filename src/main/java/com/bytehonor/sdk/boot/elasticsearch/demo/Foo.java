package com.bytehonor.sdk.boot.elasticsearch.demo;

import com.bytehonor.sdk.boot.elasticsearch.core.EsEntity;

public class Foo implements EsEntity {
	private static final long serialVersionUID = -3120704050548781869L;

	private Long id;

	private String unid;

	private String name;

	private String detail;

	private Long createAt;

	public Foo() {
		this(1L, "foo", "foo is a boy, 一个帅气的小男孩，喜欢吃饭", System.currentTimeMillis());
	}

	public Foo(Long id, String name, String detail, Long createAt) {
		this.id = id;
		this.name = name;
		this.detail = detail;
		this.createAt = createAt;
		this.unid = "asdfasdf123123132sdf";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public Long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}

	@Override
	public String esid() {
		return id.toString();
	}

}
