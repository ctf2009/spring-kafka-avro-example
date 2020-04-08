package com.ctf.kafka.service;

import ctf.avro.Error;
import ctf.avro.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProducerService {

    private static final Logger LOG = LoggerFactory.getLogger(ProducerService.class);

    @Value("${kafka.producer.topic}")
    private String producerTopic;

    @Autowired
    private KafkaTemplate<String, Message> producer;

    public void process(final String messageContent, final Error errorToThrow) {
        LOG.info("Received message to process [{}]", messageContent);

        final Message.Builder message = Message.newBuilder()
                .setPriority(1)
                .setContent(messageContent);

        if (errorToThrow != null) {
            message.setError(errorToThrow);
        }

        LOG.info("Generated Message: [{}]", message);
        producer.send("test-topic", message.build());
    }

}

