package com.ctf.kafka.processor;

import ctf.avro.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MessageStore {

    private static final Logger LOG = LoggerFactory.getLogger(MessageStore.class);

    private List<Message> messages = new ArrayList<>();

    public void store(final Message message) {
        LOG.info("Storing Message: [{}]", message);
        messages.add(message);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public int getMessageCount() {
        return messages.size();
    }

}


