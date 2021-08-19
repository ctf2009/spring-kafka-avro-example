package com.ctf.kafka.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/consumers")
public class ContainerPauseController {

    private final KafkaListenerEndpointRegistry registry;

    @GetMapping(path = "/pause", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> consumersPause() {
        var containers = this.registry.getAllListenerContainers();
        this.registry.getAllListenerContainers().forEach(MessageListenerContainer::pause);
        return new ResponseEntity<>("Accepted", HttpStatus.ACCEPTED);
    }

    @GetMapping(path = "/pause/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> consumersPauseStatus() {
        final Map<String, String> statusMap =
                this.registry.getAllListenerContainers().stream()
                        .collect(Collectors.toMap(
                                MessageListenerContainer::getListenerId,
                                m -> m.isContainerPaused() ? "paused" : "running"));
        return ResponseEntity.ok(statusMap);
    }

    @GetMapping(path = "/resume", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> consumerResume() {
        this.registry.getAllListenerContainers().forEach(MessageListenerContainer::resume);
        return new ResponseEntity<>("Accepted", HttpStatus.ACCEPTED);
    }

}
