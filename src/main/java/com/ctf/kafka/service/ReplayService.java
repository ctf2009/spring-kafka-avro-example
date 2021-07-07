package com.ctf.kafka.service;

import com.ctf.kafka.exception.UnrecoverableException;
import com.ctf.kafka.model.KafkaMeta;
import com.ctf.kafka.processor.MessageProcessor;
import ctf.avro.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ListenerUtils;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
public class ReplayService {

    private final ConsumerFactory<String, Message> consumerFactory;
    private final MessageProcessor messageProcessor;
    private final String consumerTopic;

    public void replayMessage(final KafkaMeta kafkaMeta) {
        log.info("Preparing to replay message {}", kafkaMeta);
        validateKafkaMeta(kafkaMeta);

        final var consumerRecord = getConsumerRecord(kafkaMeta);
        if (consumerRecord != null) {
            checkConsumerRecord(consumerRecord);
            processConsumerRecord(kafkaMeta, consumerRecord);
        } else {
            final var errorMessage = String.format("ConsumerRecord not found for KafkaMeta: %s", kafkaMeta);
            log.error(errorMessage);
            throw new UnrecoverableException(errorMessage);
        }
    }

    private void validateKafkaMeta(final KafkaMeta kafkaMeta) {
        if (!consumerTopic.equals(kafkaMeta.getTopic())) {
            final var errorMessage = String.format(
                    "Topic '%s' is not supported for retry in this application", kafkaMeta.getTopic());

            log.error(errorMessage);
            throw new UnrecoverableException(errorMessage);
        }
    }

    private ConsumerRecord<String, Message> getConsumerRecord(final KafkaMeta kafkaMeta) {
        log.info("Locating ConsumerRecord for KafkaMeta: {}", kafkaMeta);
        try (final Consumer<String, Message> consumer = consumerFactory.createConsumer()) {
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

    private void processConsumerRecord(final KafkaMeta kafkaMeta, final ConsumerRecord<String, Message> consumerRecord) {
        log.info("Replaying ConsumerRecord for KafkaMeta: {}", kafkaMeta);
        try {
            messageProcessor.processMessage(consumerRecord);
        } catch (final Exception exception) {
            log.error("Failed to successfully replay ConsumerRecord for KafkaMeta: {}", kafkaMeta, exception);
            throw new UnrecoverableException("Failed to replay ConsumerRecord for KafkaMeta: " + kafkaMeta, exception);
        }
    }

    private void checkConsumerRecord(final ConsumerRecord<String, Message> consumerRecord) {
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
