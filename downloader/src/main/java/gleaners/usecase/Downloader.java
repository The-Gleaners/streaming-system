package gleaners.usecase;

import gleaners.avro.DownloadTarget;
import gleaners.avro.DownloadTarget;
import org.springframework.http.HttpHeaders;
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
    private static final long NO_CONTENT_LENGTH = -1L;

    private final WebClient client = WebClient.builder()
        .clientConnector(generateConnector())
        .build();

    public Flux<String> extractLineByDelimiter(DownloadTarget downloadTarget) {
        return download(downloadTarget)
            .map(lines -> lines.split(DELIMITER))
            .flatMapMany(Flux::fromArray);
    }

    private Mono<String> download(DownloadTarget downloadTarget) {
        if(downloadTarget.getToken() != null) {
            return tokenRequest(downloadTarget);
        } else {
            return normalRequest(downloadTarget);
        }
    }

    private Mono<String> getResponseBody(
        ClientResponse response,
        DownloadTarget downloadTarget) {

        if(!isValidate(response, downloadTarget)) {
            return Mono.empty();
        }
        return response.bodyToMono(String.class);
    }

    private boolean isValidate(
        ClientResponse response,
        DownloadTarget downloadTarget) {
        long fileSize = response.headers().contentLength().orElse(NO_CONTENT_LENGTH);

        return isLargerAllowance(fileSize, downloadTarget)
            && !response.statusCode().isError();
    }

    private ReactorClientHttpConnector generateConnector() {
        return new ReactorClientHttpConnector(HttpClient.create().followRedirect(true));
    }

    private boolean isLargerAllowance(long fileSize, DownloadTarget downloadTarget) {
        if(fileSize == -1) return true;
        long allowanceSize = downloadTarget.getPrevFileSize() / 100 * downloadTarget.getMinAllowanceRate();
        return fileSize > allowanceSize;
    }


    private Mono<String> tokenRequest(DownloadTarget downloadTarget) {
        return client.get()
            .uri(URI.create(downloadTarget.getUrl()))
            .accept(MediaType.ALL)
            .exchangeToMono(response -> getResponseBody(response, downloadTarget));
    }

    private Mono<String> normalRequest(DownloadTarget downloadTarget) {
        return client.get()
            .uri(URI.create(downloadTarget.getUrl()))
            .header(HttpHeaders.AUTHORIZATION, downloadTarget.getToken())
            .accept(MediaType.ALL)
            .exchangeToMono(response -> getResponseBody(response, downloadTarget));
    }
}
