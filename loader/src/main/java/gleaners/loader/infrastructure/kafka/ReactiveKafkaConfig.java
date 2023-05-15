package gleaners.loader.infrastructure.kafka;

import gleaners.loader.domain.LoaderTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.Map;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class ReactiveKafkaConfig {

    private final KafkaProperties properties;

    @Bean
    public ReceiverOptions<String, LoaderTarget> setupReceiverOptions(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties();
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ReceiverOptions<String, LoaderTarget> basicReceiverOptions = ReceiverOptions.create(consumerProps);

        return basicReceiverOptions
                .addAssignListener(partitions -> log.debug("onPartitionAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions))
                .subscription(Collections.singletonList("test"));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, LoaderTarget> loaderTargetReceiver() {
        return new ReactiveKafkaConsumerTemplate<>(setupReceiverOptions(properties));
    }

}
