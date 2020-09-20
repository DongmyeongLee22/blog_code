package me.sun.sample.threadpool;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class ThreadPoolSimpleTest {

    AtomicInteger count = new AtomicInteger();

    @Test
    void testFixedThreadPool() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        // 100개의 작업을 수행
        IntStream.range(0, 100).forEach(i -> threadPoolExecutor.execute(getTask()));

        int poolSize = threadPoolExecutor.getPoolSize();
        int queueSize = threadPoolExecutor.getQueue().size();

        assertThat(poolSize).isEqualTo(5);
        assertThat(queueSize).isEqualTo(95);

        String message = String.format("CurrentPoolSize: %s, WorkQueueSize: %s", poolSize, queueSize);
        System.out.println(message);
        System.out.println(count.get());

        shutDownAndWaitUntilTerminated(threadPoolExecutor);
    }

    @Test
    void testCachedThreadPool() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        IntStream.range(0, 1000).forEach(i -> threadPoolExecutor.execute(getTask()));

        int poolSize = threadPoolExecutor.getPoolSize();
        int queueSize = threadPoolExecutor.getQueue().size();

        assertThat(poolSize).isEqualTo(1000);
        assertThat(queueSize).isEqualTo(0);

        TimeUnit.SECONDS.sleep(65);

        // keepAlive 시간이후엔 스레드들이 제거된다.
        assertThat(threadPoolExecutor.getPoolSize()).isEqualTo(0);

        shutDownAndWaitUntilTerminated(threadPoolExecutor);
    }

    @Test
    void testCustomThreadPoolWithTenTasks() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 15, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(5));

        IntStream.range(0, 10).forEach(i -> threadPoolExecutor.execute(getTask()));

        int poolSize = threadPoolExecutor.getPoolSize();
        int queueSize = threadPoolExecutor.getQueue().size();

        assertThat(poolSize).isEqualTo(5);
        assertThat(queueSize).isEqualTo(5);

        shutDownAndWaitUntilTerminated(threadPoolExecutor);
    }

    @Test
    void testCustomThreadPoolWithTwentyTasks() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 15, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(5));

        IntStream.range(0, 20).forEach(i -> threadPoolExecutor.execute(getTask()));

        int poolSize = threadPoolExecutor.getPoolSize();
        int queueSize = threadPoolExecutor.getQueue().size();

        assertThat(poolSize).isEqualTo(15);
        assertThat(queueSize).isEqualTo(5);

        TimeUnit.SECONDS.sleep(6);

        // keepAlive 시간이후엔 corePoolSize만큼 돌아온다.
        assertThat(threadPoolExecutor.getPoolSize()).isEqualTo(5);

        shutDownAndWaitUntilTerminated(threadPoolExecutor);
    }

    @Test
    void testCustomThreadPoolWithThirtyTasks() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 15, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(5));

        // RejectedExecutionException 발생
        Assertions.assertThrows(RejectedExecutionException.class, () -> {
            IntStream.range(0, 30).forEach(i -> threadPoolExecutor.execute(getTask()));
        });
    }

@Test
void testThreadPoolTaskExecutor()throws Exception {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(5);
    threadPoolTaskExecutor.setQueueCapacity(5);
    threadPoolTaskExecutor.setMaxPoolSize(15);
    threadPoolTaskExecutor.setKeepAliveSeconds(5);
}

    private Runnable getTask() {
        return () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                count.incrementAndGet();
                System.out.println(Thread.currentThread().getName());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void shutDownAndWaitUntilTerminated(ExecutorService executorService) {
        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
