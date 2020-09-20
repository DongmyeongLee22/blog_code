package me.sun.sample.threadpool;

public class TaskRecord {
    private final int activeCount;
    private final int queueSize;

    public TaskRecord(int activeCount, int queueSize) {
        this.activeCount = activeCount;
        this.queueSize = queueSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public int getQueueSize() {
        return queueSize;
    }
}
