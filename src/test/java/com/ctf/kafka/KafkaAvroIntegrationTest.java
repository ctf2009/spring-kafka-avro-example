package com.ctf.kafka;

import com.ctf.kafka.processor.MessageStore;
import com.ctf.kafka.service.ProducerService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@SpringBootTest
@EmbeddedKafka
@ExtendWith(MockitoExtension.class)
@DirtiesContext
class KafkaAvroIntegrationTest {

    private static final String DEFAULT_MESSAGE_CONTENT = "This is a message";

    @Autowired
    private ProducerService producerService;

    @Autowired
    private MessageStore messageStore;

    @Test
    public void sentMessageIsSuccessfullyProcessed() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        thenMessageStoreShouldHaveCount(1);
    }

    private void givenMessageIsProduced(final String messageContent) {
        this.producerService.process(messageContent);
    }

    private void thenMessageStoreShouldHaveCount(int messageCount) {
        Awaitility.await().atMost(Duration.of(5, SECONDS)).until(() ->
                this.messageStore.getMessageCount() == messageCount);
    }
}
