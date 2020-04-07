package com.ctf.kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/consumers")
public class ConsumerController {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @RequestMapping(path = "/pause",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> consumersPause() {
        this.registry.getAllListenerContainers().forEach(MessageListenerContainer::pause);
        return new ResponseEntity<>("Accepted", HttpStatus.ACCEPTED);
    }

    @RequestMapping(path = "/pause/status",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> consumersPauseStatus() {
        final Map<String, String> statusMap =
                this.registry.getAllListenerContainers().stream()
                        .collect(Collectors.toMap(
                                MessageListenerContainer::getListenerId,
                                m -> m.isContainerPaused() ? "paused" : "running"));
        return ResponseEntity.ok(statusMap);
    }

    @RequestMapping(path = "/resume",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> consumerResume() {
        this.registry.getAllListenerContainers().forEach(MessageListenerContainer::resume);
        return new ResponseEntity<>("Accepted", HttpStatus.ACCEPTED);
    }

}
