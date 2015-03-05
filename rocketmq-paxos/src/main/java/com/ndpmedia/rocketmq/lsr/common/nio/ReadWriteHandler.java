package com.ndpmedia.rocketmq.lsr.common.nio;

public interface ReadWriteHandler {
    void handleRead();

    void handleWrite();
}
