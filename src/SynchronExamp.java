public class SynchronExamp {

    public static void main(String[] args) throws InterruptedException {
        var container = new ConterContainer();
        var thread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                container.counter +=1;
            }
        });
        var thread2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                container.counter +=1;
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("Final counter value: " + container.counter);
    }
    static class ConterContainer{
        private  int counter = 0;
    }
}
