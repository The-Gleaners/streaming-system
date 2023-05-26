package gleaners.infrastructure.kafka;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record KafkaTopicProperties(
        @Value("${spring.kafka.consumer.topic}")
        String receiverTopic,
        @Value("${spring.kafka.producer.topic}")
        String senderTopic
) {
}
