package com.xf.queue;

import com.xf.common.SocketMessage;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogQueue {
    private Queue<SocketMessage> queue = new ConcurrentLinkedQueue();

    private static LogQueue logQueue = null;

    private LogQueue() {

    }

    /**
     * 单例控制
     *
     * @return 队列实例
     */
    public synchronized static LogQueue getInstance() {
        if (logQueue == null) {
            LogQueue logQueue = new LogQueue();
            LogQueue.logQueue = logQueue;
            return logQueue;
        }
        return LogQueue.logQueue;
    }

    public void addSocketMessage(SocketMessage message) {
        queue.offer(message);
    }

    /**
     * 消费日志数据 非阻塞实现
     *
     * @return socket数据
     */
    public SocketMessage getSocketMessage() {
        if (queue.size() > 0) {
            return queue.poll();
        }
        return null;
    }
}
