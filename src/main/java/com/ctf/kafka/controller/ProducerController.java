package com.ctf.kafka.controller;

import com.ctf.kafka.service.ProducerService;
import ctf.avro.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/producer")
public class ProducerController {

    @Autowired
    private ProducerService producerService;

    @RequestMapping(path = "/successful",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> successful() {
        producerService.process("Hello World " + UUID.randomUUID().toString(), null);
        return Collections.singletonMap("result", "successful");
    }

    @RequestMapping(path = "/error/recoverable",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> recoverable() {
        producerService.process("Hello World " + UUID.randomUUID().toString(), Error.RECOVERABLE);
        return Collections.singletonMap("result", "successful");
    }

    @RequestMapping(path = "/error/recoverable/nested",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> recoverableNested() {
        producerService.process("Hello World " + UUID.randomUUID().toString(), Error.NESTED_RECOVERABLE);
        return Collections.singletonMap("result", "successful");
    }

    @RequestMapping(path = "/error/unrecoverable",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> unRecoverable() {
        producerService.process("Hello World " + UUID.randomUUID().toString(), Error.UNRECOVERABLE);
        return Collections.singletonMap("result", "successful");
    }


}
