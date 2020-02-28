package com.ctf.kafka;

import com.ctf.kafka.service.ProducerService;
import com.ctf.kafka.store.MessageStore;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

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

    @Test
    @DisplayName("A message is sent, consumed and stored successfully")
    public void sentMessageIsSuccessfullyProcessed() throws Exception {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        thenMessageStoreShouldHaveCount(1);
    }

    @Test
    @DisplayName("Multiple messages are sent, consumed and stored successfully")
    public void multipleSentMessageIsSuccessfullyProcessed() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        thenMessageStoreShouldHaveCount(2);
    }

    private void givenMessageIsProduced(final String messageContent) {
        this.producerService.process(messageContent);
    }

    private void thenMessageStoreShouldHaveCount(int messageCount) {
        Awaitility.await().atMost(Duration.of(10, SECONDS)).until(() ->
                this.messageStore.getMessageCount() == messageCount);
    }
}
