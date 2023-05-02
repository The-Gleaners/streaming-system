package gleaners.usecase;

import gleaners.domain.DownloadTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class DownloaderTest {

    @Test
    @DisplayName("한줄씩 다운로드를 받을 수 있다.")
    void downloadServiceTest() {
        Downloader downloader = new Downloader();

        DownloadTarget downloadTarget = new DownloadTarget("http://httpstat.us/200");

        StepVerifier.create(downloader.response(downloadTarget))
            .thenConsumeWhile(response -> response.contains("200 OK"))
            .verifyComplete();
    }

}
