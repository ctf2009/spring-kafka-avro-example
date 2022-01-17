package com.ctf.kafka.controller;

import com.ctf.kafka.service.ProducerService;
import ctf.avro.Error;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
    public ResponseEntity<Map<String, Object>> successful() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), null);
        return successfulResponse();
    }

    @GetMapping(path = "/successful/forward", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> successfulForward() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), null, true);
        return successfulResponse();
    }

    @GetMapping(path = "/error/recoverable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> recoverable() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), Error.RECOVERABLE);
        return successfulResponse();
    }

    @GetMapping(path = "/error/recoverable/nested", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> recoverableNested() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), Error.NESTED_RECOVERABLE);
        return successfulResponse();
    }

    @GetMapping(path = "/error/unrecoverable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> unRecoverable() {
        producerService.process(MESSAGE_PREFIX + UUID.randomUUID(), Error.UNRECOVERABLE);
        return successfulResponse();
    }

    private ResponseEntity<Map<String, Object>> successfulResponse() {
        return ResponseEntity.ok(Collections.singletonMap("result", "successful"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleException(final RuntimeException e) {
        e.printStackTrace();

        return new ResponseEntity<>(Map.of(
                "status", "failed",
                "timestamp", LocalDateTime.now(),
                "exception", e.getMessage(),
                "exception-cause", ExceptionUtils.getRootCauseMessage(e)), HttpStatus.BAD_REQUEST);
    }

}
