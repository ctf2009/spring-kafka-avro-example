package com.ctf.kafka;

import com.ctf.kafka.service.ProducerService;
import com.ctf.kafka.store.MessageStore;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ActiveProfiles("integration")
@SpringBootTest
@EmbeddedKafka
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class KafkaAvroIntegrationTest {

    private static final String DEFAULT_MESSAGE_CONTENT = "This is a message";

    @Autowired
    private ProducerService producerService;

    @Autowired
    private MessageStore messageStore;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("A message is sent, consumed and stored successfully")
    void sentMessageIsSuccessfullyProcessed() throws Exception {


        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        var response = mvc.perform(get("/replay/test-topic-0-0"))
                .andReturn().getResponse();
        thenMessageStoreShouldHaveCount(1);
    }

    @Test
    @DisplayName("Multiple messages are sent, consumed and stored successfully")
    void multipleSentMessageIsSuccessfullyProcessed() {
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        givenMessageIsProduced(DEFAULT_MESSAGE_CONTENT);
        thenMessageStoreShouldHaveCount(2);
    }

    private void givenMessageIsProduced(final String messageContent) {
        this.producerService.process(messageContent, null);
    }

    private void thenMessageStoreShouldHaveCount(int messageCount) {
        Awaitility.await().atMost(Duration.of(10, SECONDS)).until(() ->
                this.messageStore.count() == messageCount);
    }
}
