package com.ctf.kafka.processor;

import com.ctf.kafka.exception.RecoverableException;
import com.ctf.kafka.exception.UnrecoverableException;
import com.ctf.kafka.store.MessageStore;
import com.ctf.kafka.store.model.MessageEntity;
import ctf.avro.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    @Autowired
    private MessageStore messageStore;

    public void processMessage(final ConsumerRecord<String, Message> record) {
        if (record.value().getError() != null) {
            switch (record.value().getError()) {
                case RECOVERABLE:
                    LOG.info("Throwing RecoverableException for Record {}", record);
                    throw new RecoverableException("Recoverable Exception");

                case NESTED_RECOVERABLE:
                    LOG.info("Throwing RecoverableException for Record {}", record);
                    throw new RuntimeException("Runtime Exception", new RecoverableException("Nested Recoverable Exception"));

                case UNRECOVERABLE:
                    LOG.info("Throwing UnrecoverableException for Record {}", record);
                    throw new UnrecoverableException("UnrecoverableException Exception");
            }
        } else {
            final MessageEntity saved = messageStore.save(toMessageEntity(record.value()));
            LOG.info("Stored Message with Id: {}", saved.getId());
        }
    }

    private MessageEntity toMessageEntity(final Message message) {
        final MessageEntity entity = new MessageEntity();
        entity.setPriority(message.getPriority());
        entity.setMessage(message.getContent());

        return entity;
    }

}
