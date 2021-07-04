package com.ctf.kafka.config;

import com.ctf.kafka.service.ProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.producer.auto-producer-enabled", havingValue = "true")
public class KafkaAutoProducerConfiguration {

    private final ProducerService producerService;
    private final AtomicLong counter = new AtomicLong();

    @Scheduled(fixedDelay = 10000)
    public void scheduleTask() {
        log.info("Scheduling Task Running");
        final long count = counter.incrementAndGet();

        final var message = String.format("Message Number: %s", count);
        producerService.process(message,null);
    }

}
