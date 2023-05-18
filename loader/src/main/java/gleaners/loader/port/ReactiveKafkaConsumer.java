package gleaners.loader.port;

import gleaners.loader.domain.LoaderTarget;
import gleaners.loader.infrastructure.kafka.ReceiverKafkaException;
import gleaners.loader.usecase.LoaderTargetRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

@Log4j2
@Component
public class ReactiveKafkaConsumer {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration MIN_BACKOFF = Duration.ofSeconds(2);
    private final ReactiveKafkaConsumerTemplate<String, LoaderTarget> reactiveKafkaConsumerTemplate;
    private final LoaderTargetRepository loaderTargetRepository;

    public ReactiveKafkaConsumer(
            ReactiveKafkaConsumerTemplate<String, LoaderTarget> reactiveKafkaConsumerTemplate,
            LoaderTargetRepository loaderTargetRepository) {
        this.reactiveKafkaConsumerTemplate = reactiveKafkaConsumerTemplate;
        this.loaderTargetRepository = loaderTargetRepository;
        start();
    }

    public void start() {
        reactiveKafkaConsumerTemplate.receive()
                .doOnNext(message -> saveToMongo(message).subscribe())
                .doOnError(log::error)
                .retryWhen(Retry.backoff(MAX_ATTEMPTS, MIN_BACKOFF)
                        .transientErrors(true))
                .onErrorResume(this::handleOnError)
                .repeat()
                .subscribe();
    }

    private Mono<ReceiverRecord<String, LoaderTarget>> handleOnError(Throwable error) {
        ReceiverKafkaException ex = (ReceiverKafkaException) error.getCause();

        log.error("Retries exhausted for : {}", ex.getRecord().value());

        ex.getRecord()
                .receiverOffset()
                .acknowledge();

        return Mono.empty();
    }

    private Mono<LoaderTarget> saveToMongo(ReceiverRecord<String, LoaderTarget> message) {
        return loaderTargetRepository.save(message.value())
                .doOnSuccess(
                        success -> {
                            log.info("Saved message: key={} value={} \n",
                                    success.id(),
                                    success.value());

                            message.receiverOffset().acknowledge();

                            log.info("Received message: topic-partition={} offset={} \n",
                                    message.receiverOffset().topicPartition(),
                                    message.receiverOffset().offset());
                        })
                .doOnError(
                        error -> log.info(error.getMessage())
                );
    }

}
