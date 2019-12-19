package com.bytehonor.sdk.boot.elasticsearch.core;

public interface EsWriteListener {

    public void onFinished(EsWriteResult result);
}
