package com.ctf.kafka.controller;

import com.ctf.kafka.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/kafka")
public class ProducerController {

    @Autowired
    private ProducerService producerService;

    @RequestMapping(path = "/publish",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> publish() {
        producerService.process("Hello World " + UUID.randomUUID().toString());
        return Collections.singletonMap("result", "successful");
    }

}
