package com.ctf.kafka.listener;

import com.ctf.kafka.exception.RecoverableException;
import com.ctf.kafka.exception.UnrecoverableException;
import com.ctf.kafka.model.KafkaMeta;
import com.ctf.kafka.processor.MessageProcessor;
import ctf.avro.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {

    private final MessageProcessor messageProcessor;

    @KafkaListener(topics = "${kafka.topics.main-topic}")
    public void listen(final ConsumerRecord<String, Message> consumerRecord,
                       final Acknowledgment acknowledgment) {
        final var kafkaMeta = KafkaMeta.fromConsumerRecord(consumerRecord);
        log.info("Received Message: Meta:{}, Value: {}", kafkaMeta, consumerRecord.value());

        process(consumerRecord);
        acknowledgment.acknowledge();
    }

    private void process(final ConsumerRecord<String, Message> consumerRecord) {
        try {
            this.messageProcessor.processMessage(consumerRecord);
        } catch (final RecoverableException | UnrecoverableException e) {
            throw e;
        } catch (final Exception e) {
            throw new UnrecoverableException("Unrecoverable Exception: ", e);
        }
    }

}
