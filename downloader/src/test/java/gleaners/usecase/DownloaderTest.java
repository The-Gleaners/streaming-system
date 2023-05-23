package gleaners.usecase;

import gleaners.avro.DownloadTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class DownloaderTest {

    @Test
    @DisplayName("한줄씩 다운로드를 받을 수 있다.")
    void downloadServiceTest() {
        Downloader downloader = new Downloader();

        DownloadTarget downloadTarget = new DownloadTarget("1", "http://httpstat.us/200", null, -1L, 0);

        StepVerifier.create(downloader.extractLineByDelimiter(downloadTarget))
            .thenConsumeWhile(response -> response.contains("200 OK"))
            .verifyComplete();
    }

}
