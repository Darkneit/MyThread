public class Main {

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new MyThread();
        Thread thread2 = new TyThread();
        Thread thread3 = new Thread(new MyRunnableThread());

        Thread thread4 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                System.out.println("Hello world! MyRunnable Thread " + i);
                try {
                    Thread.sleep(10); // Затримка на 100 мілісекунд
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }); // Runnable
        thread.start();
        thread.join();
        thread2.start();
        thread2.join();
        thread3.start();
        thread3.join();
        thread4.start();
        thread4.join();
    }
    // Створення потоку за допомогою класу Thread
    static class MyThread extends Thread {
        public void run() {
            for (int i = 0; i < 100; i++) {
                System.out.println("Hello world! MyThread " + i);
                try {
                    Thread.sleep(10); // Затримка на 100 мілісекунд
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static class TyThread extends Thread {
        public void run() {
            for (int i = 0; i < 100; i++) {
                System.out.println("Hello world! TyThread " + i);
                try {
                    Thread.sleep(10); // Затримка на 10 мілісекунд
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static class MyRunnableThread implements Runnable {
        public void run() {
            System.out.println("Hello world! MyRunnableThread");
        }
    }
}
