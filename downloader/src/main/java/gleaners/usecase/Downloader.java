package gleaners.usecase;

import gleaners.domain.DownloadTarget;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;


@Component
public class Downloader {
    private static final String DELIMITER = "\\n";

    private final WebClient client = WebClient.builder()
        .clientConnector(generateConnector())
        .build();

    public Flux<String> response(DownloadTarget requestTargetUrl) {
        return download(requestTargetUrl)
            .map(lines -> lines.split(DELIMITER))
            .flatMapMany(Flux::fromArray);
    }

    private Mono<String> download(DownloadTarget requestTargetUrl) {
        return client.get()
            .uri(URI.create(requestTargetUrl.url()))
            .accept(MediaType.ALL)
            .exchangeToMono(this::validate);
    }

    private Mono<String> validate(ClientResponse response) {
        if(response.statusCode().isError()) {
            return Mono.empty();
        }

        return response.bodyToMono(String.class);
    }

    private ReactorClientHttpConnector generateConnector() {
        return new ReactorClientHttpConnector(HttpClient.create().followRedirect(true));
    }

}
