package gleaners.loader.port;

import gleaners.loader.domain.LoaderTarget;
import gleaners.loader.infrastructure.kafka.ReceiverKafkaException;
import gleaners.loader.usecase.LoaderTargetRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Consumer;

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
                .doOnNext(processReceiveMessage())
                .doOnError(log::error)
                .retryWhen(Retry.backoff(MAX_ATTEMPTS, MIN_BACKOFF)
                        .transientErrors(true))
                .onErrorResume(this::handleOnError)
                .repeat()
                .doOnNext(message -> saveToMongo(message).subscribe())
//                .doOnNext(this::saveToMongo)
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

    private Consumer<ReceiverRecord<String, LoaderTarget>> processReceiveMessage() {
        return receiverRecord -> {
            ReceiverOffset offset = receiverRecord.receiverOffset();

            //TODO : 향후 사용 예정
            //Instant timestamp = Instant.ofEpochMilli(record.timestamp());

            offset.acknowledge();

            log.info("Received message: topic-partition={} offset={} key={} value={}\n",
                    offset.topicPartition(),
                    offset.offset(),
                    receiverRecord.key(),
                    receiverRecord.value());
        };
    }

    private Mono<LoaderTarget> saveToMongo(ReceiverRecord<String, LoaderTarget> message) {
      return  loaderTargetRepository.save(message.value())
                .doOnSuccess(
                        success ->
                                log.info("Saved message: key={} value={} \n",
                                        success.id(),
                                        success.value())
                )
                .doOnError(
                        error -> log.info(error.getMessage())
                );
    }
}
