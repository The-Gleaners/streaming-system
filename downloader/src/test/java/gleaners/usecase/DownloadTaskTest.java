package gleaners.usecase;

import gleaners.domain.DownloadTarget;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DownloadTaskTest {
    @Test
    void urlDownloadTest() {

        DownloadTask downloadTask = new DownloadTask(Mockito.any(), Mockito.any());

        DownloadTarget downloadTarget = new DownloadTarget("http://naver.com");

        downloadTask.download(downloadTarget);
    }
}
