package com.ctf.kafka.config;

import com.ctf.kafka.service.ReplayService;
import ctf.avro.Message;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@ConditionalOnProperty(name = "kafka.replay-enabled", havingValue = "true")
public class KafkaReplayConfiguration {

    @Bean
    public ReplayService replayService(final KafkaProperties kafkaProperties,
                                       final KafkaListenerEndpointRegistry registry) {
        final var consumerFactory = consumerFactory(kafkaProperties);
        return new ReplayService(consumerFactory, registry);
    }

    private DefaultKafkaConsumerFactory<String, Message> consumerFactory(final KafkaProperties kafkaProperties) {
        final var consumerProperties = kafkaProperties.buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(consumerProperties);
    }

}
