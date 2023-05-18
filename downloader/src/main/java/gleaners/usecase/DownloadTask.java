package gleaners.usecase;

import gleaners.avro.DownloadTarget;
import gleaners.avro.Product;
import gleaners.port.ProductSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class DownloadTask {

    private final ProductSender sender;
    private final Downloader downloader;

    public void downloadAndSend(DownloadTarget targetUrl) {
        downloader.extractLineByDelimiter(targetUrl)
                        .next()
                        .map(this::toProduct)
                        .subscribe(sender::send);
    }

    private Product toProduct(String row) {
        return new Product();
    }
}
