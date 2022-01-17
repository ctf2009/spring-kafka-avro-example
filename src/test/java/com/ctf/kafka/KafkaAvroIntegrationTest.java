package com.ctf.kafka;

import com.ctf.kafka.processor.MessageProcessor;
import com.ctf.kafka.service.ProducerService;
import com.ctf.kafka.store.MessageStore;
import ctf.avro.Error;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ActiveProfiles("integration")
@SpringBootTest
@EmbeddedKafka
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class KafkaAvroIntegrationTest {

    private static final String DEFAULT_MESSAGE_CONTENT = "This is a message";

    @Autowired
    private ProducerService producerService;

    @Autowired
    private MessageStore messageStore;

    @SpyBean
    private MessageProcessor messageProcessor;

    @Test
    @DisplayName("Message consumed and stored successfully")
    void messageSentAndStoredSuccessfully() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        thenMessageStoreShouldHaveCount(1);
    }

    @Test
    @DisplayName("Multiple messages are sent, consumed and stored successfully")
    void multipleSentMessageIsSuccessfullyProcessed() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        thenMessageStoreShouldHaveCount(3);
    }

    @Test
    @DisplayName("RecoverableException is retried correct number of times")
    void recoverableExceptionIsRetriedCorrectNumberOfTimes() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT, Error.RECOVERABLE);

        // 4 = Initial Attempt + 3 Retries
        verify(messageProcessor, timeout(10000).times(4)).processMessage(any());
    }

    @Test
    @DisplayName("UnrecoverableException is never retried")
    void unrecoverableExceptionIsNeverRetried() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT, Error.UNRECOVERABLE);

        // 1 = Initial Attempt + 0 Retries
        verify(messageProcessor, after(10000).times(1)).processMessage(any());
    }

    @Test
    @DisplayName("Nested RecoverableException is retried correct number of times")
    void nestedRecoverableExceptionIsRetried() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT, Error.NESTED_RECOVERABLE);

        // 4 = Initial Attempt + 3 Retries
        verify(messageProcessor, timeout(10000).times(4)).processMessage(any());
    }


    private void givenMessageIsProduced(final String messageContent) {
        this.producerService.process(messageContent, null);
    }

    private void givenMessageIsProduced(final String messageContent, Error errorToThrow) {
        this.producerService.process(messageContent, errorToThrow);
    }

    private void thenMessageStoreShouldHaveCount(int messageCount) {
        Awaitility.await()
                .atMost(Duration.of(10, SECONDS))
                .pollInterval(Duration.of(2, SECONDS))
                .until(() -> this.messageStore.count() == messageCount);
    }
}
