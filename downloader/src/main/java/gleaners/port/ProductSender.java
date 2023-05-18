package gleaners.port;

import gleaners.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ProductSender {
    private final ReactiveKafkaProducerTemplate<Integer, String> downloadSender;

    public void send(Product product) {
        System.out.println("Product : " + product);
        /*
        downloadSender.send("test-after-download", product.toString())
            .doOnSuccess(senderResult ->
                    log.info("send : {} \n offset : {}", product, senderResult.recordMetadata().offset()))
            .subscribe();*/
    }
}
