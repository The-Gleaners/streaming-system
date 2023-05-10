package gleaners.infrastructure.kafka;

import gleaners.domain.DownloadTarget;
import gleaners.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties properties;

    private ReceiverOptions<Integer, DownloadTarget> setupReceiverOptions(KafkaProperties properties) {
        Map<String, Object> consumerProps = properties.buildConsumerProperties();
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ReceiverOptions<Integer, DownloadTarget> basicReceiverOptions = ReceiverOptions.create(consumerProps);

        return basicReceiverOptions
            .addAssignListener(partitions -> log.debug("onPartitionAssigned {}", partitions))
            .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions))
            .subscription(Collections.singletonList("test"));
    }

    private SenderOptions<Integer, String> setupSenderOptions(KafkaProperties properties) {
        Map<String, Object> producerProps = properties.buildProducerProperties();
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return SenderOptions.create(producerProps);
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<Integer, DownloadTarget> receiver() {
        return new ReactiveKafkaConsumerTemplate<>(setupReceiverOptions(properties));
    }

    @Bean
    public ReactiveKafkaProducerTemplate<Integer, String> sender() {
        return new ReactiveKafkaProducerTemplate<>(setupSenderOptions(properties));
    }
}
