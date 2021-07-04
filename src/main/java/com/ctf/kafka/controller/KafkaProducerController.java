package com.ctf.kafka.controller;

import com.ctf.kafka.service.ProducerService;
import ctf.avro.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/producer")
public class KafkaProducerController {

    private static final String MESSAGE_PREFIX = "Hello World ";

    private final ProducerService producerService;

    @GetMapping(path = "/successful", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> successful() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID().toString(), null);
        return successfulResponse();
    }

    @GetMapping(path = "/error/recoverable", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> recoverable() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), Error.RECOVERABLE);
        return successfulResponse();
    }

    @GetMapping(path = "/error/recoverable/nested", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> recoverableNested() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), Error.NESTED_RECOVERABLE);
        return successfulResponse();
    }

    @GetMapping(path = "/error/unrecoverable", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> unRecoverable() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), Error.UNRECOVERABLE);
        return successfulResponse();
    }

    private Map<String, String> successfulResponse() {
        return Collections.singletonMap("result", "successful");
    }

}
