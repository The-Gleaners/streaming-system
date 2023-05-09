package gleaners.loader.infrastructure.kafka;

import reactor.kafka.receiver.ReceiverRecord;

@SuppressWarnings("serial")
public class ReceiverKafkaException extends RuntimeException {
    private final ReceiverRecord receiverRecord;

    ReceiverKafkaException(ReceiverRecord receiverRecord, Throwable throwable)
    {
        super(throwable);
        this.receiverRecord = receiverRecord;
    }

    public ReceiverRecord getRecord()
    {
        return this.receiverRecord;
    }
}
