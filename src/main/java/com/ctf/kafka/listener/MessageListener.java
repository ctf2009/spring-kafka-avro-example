package com.ctf.kafka.listener;

import com.ctf.kafka.store.MessageStore;
import com.ctf.kafka.store.model.MessageEntity;
import ctf.avro.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Autowired
    private MessageStore messageStore;

    private static final Logger LOG = LoggerFactory.getLogger(MessageListener.class);

    @KafkaListener(topics = "${kafka.consumer.topic}")
    public void listen(ConsumerRecord<String, Message> record) {

        final String recordStringMeta = String.format("%s-%d-%d",
                record.topic(),
                record.partition(),
                record.offset());

        LOG.info("Received Message: Meta:{}, Value: {}", recordStringMeta, record.value());

        final MessageEntity saved = messageStore.save(toMessageEntity(record.value()));

        LOG.info("Stored Message with Id: {}", saved.getId());
    }

    private MessageEntity toMessageEntity(final Message message) {
        final MessageEntity entity = new MessageEntity();
        entity.setPriority(message.getPriority());
        entity.setMessage(message.getContent());

        return entity;
    }

}
