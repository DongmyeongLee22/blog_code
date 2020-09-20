package me.sun.sample.threadpool;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ThreadPoolTest {

    @Test
    void testKeepAliveTime()throws Exception {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1));
        IntStream.range(0, 10).forEach(i -> threadPoolExecutor.execute(getTask()));

        assertThat(threadPoolExecutor.getPoolSize()).isGreaterThan(5);
        Thread.sleep(1000);
        assertThat(threadPoolExecutor.getPoolSize()).isGreaterThan(5);
        Thread.sleep(2000);
        assertThat(threadPoolExecutor.getPoolSize()).isEqualTo(5);

        shutDownAndWait(threadPoolExecutor);
    }

    @Test
    void testCachedThreadPool() throws Exception {
        List<TaskRecord> taskRecords = runTasks(Executors.newCachedThreadPool());
        for (TaskRecord taskRecord : taskRecords) {
            assertThat(taskRecord.getQueueSize()).isEqualTo(0);
        }
    }

    @Test
    void testFixedThreadPool() throws Exception {
        List<TaskRecord> taskRecords = runTasks(Executors.newFixedThreadPool(300));
        for (TaskRecord taskRecord : taskRecords) {
            assertThat(taskRecord.getActiveCount()).isLessThanOrEqualTo(300);
        }
    }

    @Test
    void testCustomThreadPoolWithNoLimitedTaskQueue() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(300, 500,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        List<TaskRecord> taskRecords = runTasks(threadPoolExecutor);
        for (TaskRecord taskRecord : taskRecords) {
            assertThat(taskRecord.getActiveCount()).isLessThanOrEqualTo(300);
        }
    }

    @Test
    void testCustomThreadPoolWithLimitedTaskQueue() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(300, 1000,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500));
        List<TaskRecord> taskRecords = runTasks(threadPoolExecutor);
        for (TaskRecord taskRecord : taskRecords) {
            assertThat(taskRecord.getActiveCount()).isLessThanOrEqualTo(1000);
            assertThat(taskRecord.getQueueSize()).isLessThanOrEqualTo(500);
        }

        boolean existGreaterThanCorePoolSize = taskRecords.stream()
                .map(TaskRecord::getActiveCount)
                .anyMatch(count -> count > 300);
        assertThat(existGreaterThanCorePoolSize).isTrue();
    }

    private List<TaskRecord> runTasks(ExecutorService executorService) throws ExecutionException, InterruptedException {
        Future<List<TaskRecord>> taskRecordFuture = startRecordingThreadPoolStatus((ThreadPoolExecutor) executorService);
        try {
            IntStream.rangeClosed(1, 1500).forEach(i -> executorService.execute(getTask()));
        } finally {
            shutDownAndWait(executorService);
        }
        return taskRecordFuture.get();
    }

    private Future<List<TaskRecord>> startRecordingThreadPoolStatus(ThreadPoolExecutor threadPoolExecutor) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        return executorService.submit(() -> logging(threadPoolExecutor));
    }

    private List<TaskRecord> logging(ThreadPoolExecutor threadPoolExecutor) throws InterruptedException {
        List<TaskRecord> taskRecords = new ArrayList<>();

        while (!threadPoolExecutor.isTerminated()) {
            int activeCount = threadPoolExecutor.getActiveCount();
            int queueSize = threadPoolExecutor.getQueue().size();
            taskRecords.add(new TaskRecord(activeCount, queueSize));
            TimeUnit.MILLISECONDS.sleep(10);
        }

        return taskRecords;
    }


    private Runnable getTask() {
        return () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void shutDownAndWait(ExecutorService executorService) {
        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
