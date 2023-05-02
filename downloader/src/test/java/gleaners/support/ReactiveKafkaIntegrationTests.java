package gleaners.support;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.kafka.test.condition.EmbeddedKafkaCondition;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EmbeddedKafka(
    topics = ReactiveKafkaIntegrationTests.REACTIVE_INT_KEY_TOPIC,
    brokerProperties = { "transaction.state.log.replication.factor=1", "transaction.state.log.min.isr=1" }
)
public class ReactiveKafkaIntegrationTests {
    public static final String REACTIVE_INT_KEY_TOPIC = "reactive_int_key_topic";
    private static final int DEFAULT_PARTITIONS_COUNT = 2;
    private static final int DEFAULT_KEY = 42;
    private static final String DEFAULT_VALUE = "foo_data";
    private static final int DEFAULT_PARTITION = 1;
    private static final long DEFAULT_TIMESTAMP = Instant.now().toEpochMilli();
    private static final Duration DEFAULT_VERIFY_TIMEOUT = Duration.ofSeconds(10);

    private static ReactiveKafkaConsumerTemplate<Integer, String> reactiveKafkaConsumerTemplate;

    private ReactiveKafkaProducerTemplate<Integer, String> reactiveKafkaProducerTemplate;

    @BeforeAll
    public static void setUpBeforeClass() {
        Map<String, Object> consumerProps = KafkaTestUtils
            .consumerProps("reactive_consumer_group", "false", EmbeddedKafkaCondition.getBroker());
        reactiveKafkaConsumerTemplate =
            new ReactiveKafkaConsumerTemplate<>(setupReceiverOptionsWithDefaultTopic(consumerProps));
    }

    @BeforeEach
    public void setUp() {
        this.reactiveKafkaProducerTemplate = new ReactiveKafkaProducerTemplate<>(setupSenderOptionsWithDefaultTopic(),
            new MessagingMessageConverter());
    }

    @Test
    public void shouldSendSingleRecordAsKeyAndReceiveIt() {
        Mono<SenderResult<Void>> senderResultMono =
            this.reactiveKafkaProducerTemplate.send(REACTIVE_INT_KEY_TOPIC, DEFAULT_VALUE);

        StepVerifier.create(senderResultMono)
            .assertNext(senderResult -> {
                assertEquals(senderResult.recordMetadata().topic(), REACTIVE_INT_KEY_TOPIC);
            })
            .expectComplete()
            .verify(DEFAULT_VERIFY_TIMEOUT);

        StepVerifier.create(reactiveKafkaConsumerTemplate.receive().doOnNext(rr -> rr.receiverOffset().acknowledge()))
            .assertNext(receiverRecord -> assertEquals(receiverRecord.value(), DEFAULT_VALUE))
            .thenCancel()
            .verify(DEFAULT_VERIFY_TIMEOUT);
    }


    private static ReceiverOptions<Integer, String> setupReceiverOptionsWithDefaultTopic(
        Map<String, Object> consumerProps) {

        ReceiverOptions<Integer, String> basicReceiverOptions = ReceiverOptions.create(consumerProps);
        return basicReceiverOptions
            .consumerProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            .addAssignListener(p -> assertEquals(p.iterator().next().topicPartition().topic(), REACTIVE_INT_KEY_TOPIC))
            .subscription(Collections.singletonList(REACTIVE_INT_KEY_TOPIC));
    }

    private SenderOptions<Integer, String> setupSenderOptionsWithDefaultTopic() {
        Map<String, Object> senderProps =
            KafkaTestUtils.producerProps(EmbeddedKafkaCondition.getBroker().getBrokersAsString());
        return SenderOptions.create(senderProps);
    }
}
