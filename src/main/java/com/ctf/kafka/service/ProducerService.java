package com.ctf.kafka.service;

import ctf.avro.Error;
import ctf.avro.Message;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProducerService {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerService.class);

    @Value("${kafka.topics.main-topic}")
    private String producerTopic;

    private final KafkaTemplate<String, Message> producer;

    public void process(final String messageContent, final Error errorToThrow) {
        process(messageContent, errorToThrow, false);
    }

    public void process(final String messageContent, final Error errorToThrow, final boolean setForwarding) {
        LOG.info("Received message to process [{}]", messageContent);

        final Message.Builder message = Message.newBuilder()
                .setPriority(1)
                .setContent(messageContent);

        if (errorToThrow != null) {
            message.setError(errorToThrow);
        }

        if (setForwarding) {
            message.setForward(true);
        }

        process(message.build(), producerTopic);
    }

    public void process(final Message message, final String topic) {
        producer.send(topic, message);
    }

}

