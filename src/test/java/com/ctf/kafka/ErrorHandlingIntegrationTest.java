package com.ctf.kafka;

import com.ctf.kafka.processor.MessageProcessor;
import com.ctf.kafka.service.ProducerService;
import com.ctf.kafka.store.MessageStore;
import ctf.avro.Error;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ActiveProfiles("integration")
@SpringBootTest
@EmbeddedKafka
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@EnableAutoConfiguration(exclude = {HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
class ErrorHandlingIntegrationTest {

    private static final String DEFAULT_MESSAGE_CONTENT = "This is a message";

    @Autowired
    private ProducerService producerService;

    @SpyBean
    private MessageProcessor messageProcessor;

    @MockBean
    private MessageStore messageStore;

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

    private void givenMessageIsProduced(final String messageContent, Error errorToThrow) {
        this.producerService.process(messageContent, errorToThrow);
    }
}
