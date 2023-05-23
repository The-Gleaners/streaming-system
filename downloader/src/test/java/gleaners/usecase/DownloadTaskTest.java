package gleaners.usecase;

import gleaners.avro.DownloadTarget;
import gleaners.support.ReactiveKafkaIntegrationTests;
import lombok.extern.log4j.Log4j2;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;

import java.io.IOException;

@Log4j2
class DownloadTaskTest extends ReactiveKafkaIntegrationTests {

    public static MockWebServer mockWebServer;

    static {
        BlockHound.builder()
            .disallowBlockingCallsInside(DownloadTask.class.getName(), "downloadAndSend")
            .blockingMethodCallback(method -> {
                String message = String.format("[%s] Blocking call! %s", Thread.currentThread(), method);
                System.out.println(message);
            })
            .install();
    }

    @BeforeAll
    static void setUpMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownMockServer() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    void urlDownloadTest() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
            .setBody("test1\ntest2")
            .addHeader("Content-Type", "application/json"));


//        ProductSender productSender = new ProductSender(reactiveKafkaProducerTemplate);

        Downloader downloader = new Downloader();

//        DownloadTask downloadTask = new DownloadTask(productSender, downloader);

        DownloadTarget downloadTarget = new DownloadTarget("1", "https://www.naver.com", "test-token", -1L, 0);


//        downloadTask
//            .downloadAndSend(downloadTarget);
    }
}
