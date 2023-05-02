package gleaners.usecase;

import gleaners.domain.DownloadTarget;
import gleaners.domain.Product;
import gleaners.port.KafkaSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DownloadTask {
    private final KafkaSender sender;
    private final Downloader downloader;

    public void downloadAndSend(DownloadTarget targetUrl) {
        downloader.response(targetUrl)
            .map(Product::new)
            .subscribe(sender::send);
    }
}
