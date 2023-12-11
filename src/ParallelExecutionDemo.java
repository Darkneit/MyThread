import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ParallelExecutionDemo {
    private static final int THREAD_COUNT = 3;
    private static CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
    private static Semaphore semaphore = new Semaphore(1);
    private static Lock lock = new ReentrantLock();
    private static CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    simultaneousExecution();
                    restrictedParallelExecution();
                    sequentialExecution();
                    simultaneousCompletion();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown(); // Shut down the ThreadPool
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Attempt to submit a new task after the ThreadPool has been shut down
        try {
            executorService.submit(() -> System.out.println("New task after shutdown"));
        } catch (RejectedExecutionException e) {
            System.out.println("Task submission rejected. ThreadPool is shut down.");
        }
    }
    private static void simultaneousExecution() throws InterruptedException {
        System.out.println("CountDownLatch - Start");
        Thread.sleep(1000);
        System.out.println("CountDownLatch - End");//використання CountDownLatch для одночасного запуску усіх потоків
        latch.countDown();
        latch.await();
    }
    private static void restrictedParallelExecution() throws InterruptedException {
        semaphore.acquire();
        System.out.println("Semaphore - Start");
        Thread.sleep(10);
        System.out.println("Semaphore - End");//використання Semaphore для обмеження паралельного виконання до одного потоку одночасно
        semaphore.release();
        latch.countDown();
        latch.await();
    }
    private static void sequentialExecution() throws InterruptedException {
        lock.lock();
        System.out.println("ReentrantLock - Start");
        Thread.sleep(1000);
        System.out.println("ReentrantLock - End");// використання ReentrantLock для почергового виконання певної секції коду
        lock.unlock();
        latch.countDown();
        latch.await();
    }
    private static void simultaneousCompletion() throws InterruptedException {
        System.out.println("CyclicBarrier - Start");
        try {
            barrier.await();
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        System.out.println("CyclicBarrier - End"); //використання CyclicBarrier для одночасного завершення всіх потоків після виконання
    }
}


