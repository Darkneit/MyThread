import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.*;

        public class  ImageDownloader{
            private static final int THREAD_POOL_SIZE = 3;
            private static final int MAX_PARALLEL_DOWNLOADS = 2;
            private static final String[] IMAGE_URLS = {
                    "https://img.freepik.com/free-photo/beautiful-scenery-of-a-pathway-in-a-forest-with-trees-covered-with-frost_181624-42376.jpg?size=626&ext=jpg&ga=GA1.1.30829724.1702303947&semt=sph",
                    "https://img.freepik.com/free-photo/side-view-of-breakfast-table-with-sausages-fresh-vegetables-cheese-ham-and-sauces-jpg_140725-12151.jpg?size=626&ext=jpg",
                    "https://img.freepik.com/free-photo/pier-at-a-lake-in-hallstatt-austria_181624-44201.jpg?size=626&ext=jpg&ga=GA1.1.30829724.1702303947&semt=sph"
            };

            private static Semaphore semaphore = new Semaphore(MAX_PARALLEL_DOWNLOADS);
            private static CyclicBarrier barrier;
            private static final Object lock = new Object();
            private static long minFileSize = Long.MAX_VALUE;
            private static long maxFileSize = Long.MIN_VALUE;

            public static void main(String[] args) {
                barrier = new CyclicBarrier(THREAD_POOL_SIZE + 1);

                ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

                for (String imageUrl : IMAGE_URLS) {
                    executorService.submit(() -> {
                        try {
                            downloadImage(imageUrl);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
                try {
                    barrier.await(); // Wait for all worker threads to finish
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                executorService.shutdown();

                // Calculate and print average file size
                long averageFileSize = (maxFileSize + minFileSize) / 2;
                System.out.println("Average file size: " + averageFileSize + " bytes");

                // Open the folder with downloaded images
                openFolder();
            }

            private static void downloadImage(String imageUrl) throws IOException, InterruptedException {
                semaphore.acquire(); // Acquire the semaphore permit

                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long fileSize = 0;

                    Path filePath = Paths.get(getFileNameFromUrl(imageUrl));

                    try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE)) {
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            ((OutputStream) outputStream).write(buffer, 0, bytesRead);
                            fileSize += bytesRead;
                        }
                    }

                    // Synchronize on a common lock to update minFileSize and maxFileSize
                    synchronized (lock) {
                        minFileSize = Math.min(minFileSize, fileSize);
                        maxFileSize = Math.max(maxFileSize, fileSize);
                    }

                    System.out.println(Thread.currentThread().getName() + ": Downloaded " + imageUrl +
                            " | File size: " + fileSize + " bytes");

                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                } finally {
                    semaphore.release(); // Release the semaphore permit
                    try {
                        barrier.await(); // Signal that this thread has finished its work
                    } catch (BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    connection.disconnect(); // Disconnect the HTTP connection
                }
            }

            private static void openFolder() {
                String userDirectory = System.getProperty("user.dir");

                try {
                    Desktop.getDesktop().open(new File(userDirectory));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private static String getFileNameFromUrl(String url) throws URISyntaxException {
                URI uri = new URI(url);
                String path = uri.getPath();
                return Paths.get(path).getFileName().toString();
            }
        }
