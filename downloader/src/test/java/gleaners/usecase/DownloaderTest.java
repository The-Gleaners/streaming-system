package gleaners.usecase;

import gleaners.domain.DownloadTarget;
import gleaners.port.ProductSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class DownloaderTest {

    @Test
    @DisplayName("한줄씩 다운로드를 받을 수 있다.")
    void downloadServiceTest() {
        Downloader downloader = new Downloader();

        DownloadTarget downloadTarget = new DownloadTarget("http://httpstat.us/200", null);

        StepVerifier.create(downloader.extractLineByDelimiter(downloadTarget))
            .thenConsumeWhile(response -> response.contains("200 OK"))
            .verifyComplete();
    }

    @Test
    void test() {
        ProductSender productSender = new ProductSender(null);

        Downloader downloader = new Downloader();
        Parser tsvConvertor = new Parser();

        DownloadTask downloadTask = new DownloadTask(productSender, downloader, tsvConvertor);

        DownloadTarget downloadTarget = new DownloadTarget("https://korea-ne-wmf-ep-production.s3.ap-northeast-2.amazonaws.com/89f4a4733fd21df33816e84e97d8eb40/ep_total.tsv");

        downloadTask
            .downloadAndSend(downloadTarget);
    }
}
