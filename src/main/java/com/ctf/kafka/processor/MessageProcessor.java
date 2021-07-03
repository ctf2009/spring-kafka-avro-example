package com.ctf.kafka.processor;

import com.ctf.kafka.exception.CustomRuntimeException;
import com.ctf.kafka.exception.RecoverableException;
import com.ctf.kafka.exception.UnrecoverableException;
import com.ctf.kafka.store.MessageStore;
import com.ctf.kafka.store.model.MessageEntity;
import ctf.avro.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProcessor {

    private final MessageStore messageStore;

    @Transactional
    public void processMessage(final ConsumerRecord<String, Message> consumerRecord) {
        if (consumerRecord.value().getError() != null) {
            switch (consumerRecord.value().getError()) {
                case RECOVERABLE -> {
                    log.info("Throwing RecoverableException for Record {}", consumerRecord);
                    throw new RecoverableException("Recoverable Exception");
                }
                case NESTED_RECOVERABLE -> {
                    log.info("Throwing RecoverableException for Record {}", consumerRecord);
                    throw new CustomRuntimeException("Runtime Exception", new RecoverableException("Nested Recoverable Exception"));
                }
                case UNRECOVERABLE -> {
                    log.info("Throwing UnrecoverableException for Record {}", consumerRecord);
                    throw new UnrecoverableException("UnrecoverableException Exception");
                }
                default -> throw new UnrecoverableException("Invalid Error Value");
            }
        } else {
            final MessageEntity saved = messageStore.save(toMessageEntity(consumerRecord));
            log.info("Stored Message {}", saved);
        }
    }

    private MessageEntity toMessageEntity(final ConsumerRecord<String, Message> consumerRecord) {
        final var message = consumerRecord.value();
        return MessageEntity.builder()
                .priority(message.getPriority())
                .message(message.getContent())
                .topic(consumerRecord.topic())
                .partition(consumerRecord.partition())
                .offset(consumerRecord.offset())
                .build();
    }

}
