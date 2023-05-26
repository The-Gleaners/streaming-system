package gleaners.infrastructure.kafka;

import gleaners.avro.DownloadTarget;
import gleaners.avro.Product;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaProperties properties;

    private final KafkaTopicProperties topicProperties;

    private ReceiverOptions<String, DownloadTarget> setupReceiverOptions(KafkaProperties properties) {
        Map<String, Object> consumerProps = properties.buildConsumerProperties();
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        ReceiverOptions<String, DownloadTarget> basicReceiverOptions = ReceiverOptions.create(consumerProps);

        return basicReceiverOptions
            .addAssignListener(partitions -> log.debug("onPartitionAssigned {}", partitions))
            .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions))
            .subscription(Collections.singletonList(topicProperties.receiverTopic()));
    }

    private SenderOptions<String, Product> setupSenderOptions(KafkaProperties properties) {
        Map<String, Object> producerProps = properties.buildProducerProperties();
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProps.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, false);
        producerProps.put(AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION, true);

        return SenderOptions.create(producerProps);
    }


    @Bean
    public ReactiveKafkaConsumerTemplate<String, DownloadTarget> receiver() {
        return new ReactiveKafkaConsumerTemplate<>(setupReceiverOptions(properties));
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, Product> sender() {
        return new ReactiveKafkaProducerTemplate<>(setupSenderOptions(properties));
    }
}
