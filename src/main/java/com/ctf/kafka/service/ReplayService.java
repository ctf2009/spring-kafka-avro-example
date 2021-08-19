package com.ctf.kafka.service;

import com.ctf.kafka.exception.UnrecoverableException;
import com.ctf.kafka.model.KafkaMeta;
import ctf.avro.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
public class ReplayService {

    private final Consumer<?, ?> consumer;
    private final KafkaListenerEndpointRegistry registry;

    public ReplayService(final ConsumerFactory<?,?> consumerFactory, final KafkaListenerEndpointRegistry registry) {
        this.consumer = consumerFactory.createConsumer();
        this.registry = registry;
    }

    public void replayMessage(final KafkaMeta kafkaMeta) {
        log.info("Preparing to replay message {}", kafkaMeta);

        final var consumerRecord = getConsumerRecord(kafkaMeta);
        if (consumerRecord != null) {
            checkConsumerRecord(consumerRecord);
            registry.getAllListenerContainers().stream()
                    .map(MessageListenerContainer::getContainerProperties)
                    .filter(properties -> Arrays.asList(Objects.requireNonNull(properties.getTopics())).contains(kafkaMeta.getTopic()))
                    .map(ContainerProperties::getMessageListener)
                    .forEach(listener -> processConsumerRecord(listener, kafkaMeta, consumerRecord));
        } else {
            final var errorMessage = String.format("ConsumerRecord not found for KafkaMeta: %s", kafkaMeta);
            log.error(errorMessage);
            throw new UnrecoverableException(errorMessage);
        }
    }

    private ConsumerRecord<?, ?> getConsumerRecord(final KafkaMeta kafkaMeta) {
        log.info("Locating ConsumerRecord for KafkaMeta: {}", kafkaMeta);
        try {
            final var topicPartition = new TopicPartition(kafkaMeta.getTopic(), kafkaMeta.getPartition());
            consumer.assign(Collections.singletonList(topicPartition));
            consumer.seek(topicPartition, kafkaMeta.getOffset());

            final var consumerRecords = consumer.poll(Duration.of(5, ChronoUnit.SECONDS));
            return StreamSupport.stream(consumerRecords.spliterator(), false)
                    .findFirst()
                    .filter(cr -> cr.offset() == kafkaMeta.getOffset())
                    .orElse(null);

        } catch (final Exception exception) {
            log.error("Exception whilst attempting to locate ConsumerRecord:", exception);
            final var errorMessage =
                    String.format("There was an Exception whilst attempting to locate the ConsumerRecord for KafkaMeta: %s", kafkaMeta);

            log.error(errorMessage);
            throw new UnrecoverableException(errorMessage, exception);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processConsumerRecord(final Object messageListener, final KafkaMeta kafkaMeta, final ConsumerRecord<?, ?> consumerRecord) {
        log.info("Replaying ConsumerRecord for KafkaMeta: {}", kafkaMeta);
        try {
            if (AcknowledgingConsumerAwareMessageListener.class.isAssignableFrom(messageListener.getClass())) {
                ((AcknowledgingConsumerAwareMessageListener) messageListener).onMessage(consumerRecord, new NoopAcknowledgement(), consumer);
            } else if (AcknowledgingMessageListener.class.isAssignableFrom(messageListener.getClass())) {
                ((AcknowledgingMessageListener) messageListener).onMessage(consumerRecord, new NoopAcknowledgement());
            } else if (ConsumerAwareMessageListener.class.isAssignableFrom(messageListener.getClass())) {
                ((ConsumerAwareMessageListener) messageListener).onMessage(consumerRecord, consumer);
            } else if (MessageListener.class.isAssignableFrom(messageListener.getClass())) {
                ((MessageListener) messageListener).onMessage(consumerRecord);
            } else {
                throw new UnrecoverableException("Message Listener is unsupported");
            }
        } catch (final Exception exception) {
            log.error("Failed to successfully replay ConsumerRecord for KafkaMeta: {}", kafkaMeta, exception);
            throw new UnrecoverableException("Failed to replay ConsumerRecord for KafkaMeta: " + kafkaMeta, exception);
        }
    }

    private void checkConsumerRecord(final ConsumerRecord<?, ?> consumerRecord) {
        if (consumerRecord.value() == null) {
            final var logger = new LogAccessor(LogFactory.getLog(ReplayService.class));
            final var deserializationException =
                    ListenerUtils.getExceptionFromHeader(consumerRecord, ErrorHandlingDeserializer.VALUE_DESERIALIZER_EXCEPTION_HEADER, logger);

            if(deserializationException != null) {
                log.error("There was an Exception Deserializing the Record: ", deserializationException);
                throw new UnrecoverableException("Deserialization Exception", deserializationException);
            } else {
                throw new UnrecoverableException("Not Replaying Record as the Record value is null");
            }
        }
    }
}
