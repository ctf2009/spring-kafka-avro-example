package com.ctf.kafka.config;

import com.ctf.kafka.exception.RecoverableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaListenerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaListenerConfiguration.class);

    @Value("${retry.consumer.backoff-ms:1000}")
    private int backoffMillis;

    @Value("${retry.consumer.max-attempts:10}")
    private int retryCount;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer factoryConfigurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factoryConfigurer.configure(factory, kafkaConsumerFactory);

        // No Longer Need to use RetryTemplate & Recovery Callback
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    private SeekToCurrentErrorHandler errorHandler() {
        final var seekToCurrentErrorHandler = new SeekToCurrentErrorHandler((consumerRecord, exception) ->
                        LOG.warn("Handing Error for consumerRecord {} with exception {}", consumerRecord, exception.getClass()),
                        // Do any further processing here, Once complete, the recovered Error is committed
                        new FixedBackOff(backoffMillis, retryCount));
        seekToCurrentErrorHandler.setClassifications(exceptionClassifications(), false);

        // This is required to be set to true so that failures that are handled are committed
        seekToCurrentErrorHandler.setCommitRecovered(true);
        return seekToCurrentErrorHandler;
    }

    private Map<Class<? extends Throwable>, Boolean> exceptionClassifications() {
        final Map<Class<? extends Throwable>, Boolean> classified = new HashMap<>();
        classified.put(RecoverableException.class, true);
        return classified;
    }

}
