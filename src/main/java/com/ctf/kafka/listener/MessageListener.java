package com.ctf.kafka.listener;

import com.ctf.kafka.processor.MessageProcessor;
import ctf.avro.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    private MessageProcessor messageProcessor;

    @KafkaListener(topics = "${kafka.consumer.topic}")
    public void listen(ConsumerRecord<String, Message> record, final Acknowledgment acknowledgment) {

        final String recordStringMeta = String.format("%s-%d-%d",
                record.topic(),
                record.partition(),
                record.offset());

        LOG.info("Received Message: Meta:{}, Value: {}", recordStringMeta, record.value());
        this.messageProcessor.processMessage(record);
        acknowledgment.acknowledge();
    }

}
