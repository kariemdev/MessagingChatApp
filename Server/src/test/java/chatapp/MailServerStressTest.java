package chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.lang.management.*;

//import static java.lang.management.ManagementFactory;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;

public class MailServerStressTest {
    private MailServer mailServer = new MailServer("localhost", 27017);
    private OperatingSystemMXBean operatingSystemMXBean;
    private RuntimeMXBean runtimeMXBean ;
    private MemoryMXBean memoryMXBean ;


    @BeforeEach
    public void setUp() {
        mailServer = new MailServer("localhost", 27017);
         operatingSystemMXBean = getOperatingSystemMXBean();
         runtimeMXBean = ManagementFactory.getRuntimeMXBean();
         memoryMXBean = ManagementFactory.getMemoryMXBean();

    }

    public void printMemoryUsage() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        long heapUsedMB = heapMemoryUsage.getUsed() / 1024 / 1024;
        long heapMaxMB = heapMemoryUsage.getMax() / 1024 / 1024;

        long nonHeapUsedMB = nonHeapMemoryUsage.getUsed() / 1024 / 1024;
        long nonHeapMaxMB = nonHeapMemoryUsage.getMax() / 1024 / 1024;

        System.out.println("Heap Memory: " + heapUsedMB + "/" + heapMaxMB + " MB");
        System.out.println("Non-Heap Memory: " + nonHeapUsedMB + "/" + nonHeapMaxMB + " MB");
    }
    public void printUptime() {
        System.out.println("Uptime: " + runtimeMXBean.getUptime());
    }
    @Test
    public void stressTestSaveEmail() throws InterruptedException {

        int numberOfThreads = 10; // Number of concurrent threads/users for the stress test
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    StringBuilder emailData = new StringBuilder("Email body");
                    mailServer.saveEmail("sender" + Thread.currentThread().getId(), emailData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);
        printMemoryUsage();
        printUptime();
    }
}