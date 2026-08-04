package com.plsql2java.common;

@FunctionalInterface
public interface ProgressListener {
    void onProgress(ProgressEvent event);

    static ProgressListener noOp() {
        return event -> {};
    }
}
