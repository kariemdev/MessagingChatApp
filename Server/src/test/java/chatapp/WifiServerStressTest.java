package chatapp;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

public class WifiServerStressTest {
    class WifiClientTest implements Runnable {
        private Socket socket;
        private Scanner scanner;

        public WifiClientTest(String serverAddress, int serverPort) throws IOException {
            socket = new Socket(serverAddress, serverPort);
            scanner = new Scanner(System.in);
        }

        public void startClient() {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                while (scanner.hasNextLine()) {
                    out.println(scanner.nextLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            startClient();
        }
    }

    public void printMemoryUsage() {


        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        long heapUsedMB = heapMemoryUsage.getUsed() / 1024 / 1024;
        long heapMaxMB = heapMemoryUsage.getMax() / 1024 / 1024;

        long nonHeapUsedMB = nonHeapMemoryUsage.getUsed() / 1024 / 1024;

        System.out.println("Heap Memory: " + heapUsedMB + "/" + heapMaxMB + " MB");
        System.out.println("Non-Heap Memory: " + nonHeapUsedMB  + " MB");
    }

    @Test
    public void stressTestWifiClient() throws InterruptedException {
        int numberOfThreads = 200; // Number of concurrent threads/users for the stress test
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);


        WifiServer server = new WifiServer();
        Thread serverThread = new Thread(server);
        serverThread.start();

        // Add a delay to ensure the server is ready to accept connections
        Thread.sleep(5000);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    new WifiClientTest("localhost", 8082).startClient();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            });
        }




        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        printMemoryUsage();
    }
}