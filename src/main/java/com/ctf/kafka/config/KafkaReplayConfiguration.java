package com.ctf.kafka.config;

import com.ctf.kafka.processor.MessageProcessor;
import com.ctf.kafka.service.ReplayService;
import ctf.avro.Message;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@ConditionalOnProperty(name = "kafka.consumer.replay-enabled", havingValue = "true")
public class KafkaReplayConfiguration {

    @Bean
    public ReplayService replayService(final KafkaProperties kafkaProperties,
                                       final MessageProcessor messageProcessor,
                                       @Value("${kafka.consumer.topic}") final String topic) {
        final var consumerFactory = consumerFactory(kafkaProperties);
        return new ReplayService(consumerFactory, messageProcessor, topic);
    }

    private DefaultKafkaConsumerFactory<String, Message> consumerFactory(final KafkaProperties kafkaProperties) {
        final var consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

}
