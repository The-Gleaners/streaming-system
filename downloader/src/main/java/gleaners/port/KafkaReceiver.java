package gleaners.port;

import gleaners.domain.DownloadTarget;
import gleaners.infrastructure.kafka.ReceiverRecordException;
import gleaners.usecase.DownloadTask;
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
public class KafkaReceiver {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration MIN_BACKOFF = Duration.ofSeconds(2);

    private final ReactiveKafkaConsumerTemplate<Integer, DownloadTarget> receiver;
    private final DownloadTask downloadTask;

    public KafkaReceiver(ReactiveKafkaConsumerTemplate<Integer, DownloadTarget> receiver, DownloadTask downloadTask) {
        this.receiver = receiver;
        this.downloadTask = downloadTask;

        start();
    }

    public void start() {
        receiver.receive()
                .doOnNext(processReceiveMessage())
                .doOnError(log::error)
                .retryWhen(Retry.backoff(MAX_ATTEMPTS, MIN_BACKOFF)
                        .transientErrors(true))
                .onErrorResume(this::handleOnError)
                .repeat()
                .doOnNext(message -> downloadTask.downloadAndSend(message.value()))
                .subscribe();
    }

    private Mono<ReceiverRecord<Integer, DownloadTarget>> handleOnError(Throwable error) {
        ReceiverRecordException ex = (ReceiverRecordException) error.getCause();

        log.error("Retries exhausted for : {}", ex.getRecord().value());

        ex.getRecord()
                .receiverOffset()
                .acknowledge();

        return Mono.empty();
    }

    private Consumer<ReceiverRecord<Integer, DownloadTarget>> processReceiveMessage() {
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

}
