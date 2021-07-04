package com.ctf.kafka.controller;

import com.ctf.kafka.exception.UnrecoverableException;
import com.ctf.kafka.model.KafkaMeta;
import com.ctf.kafka.service.ReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/replay")
public class ReplayController {

    private static final Pattern COORDINATES_REGEX =
            Pattern.compile("(^[a-zA-Z][a-zA-Z-]+[a-zA-Z])-(\\d+)-(\\d+)$");

    private final ReplayService replayService;

    @GetMapping(value = "/{coordinates}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> replay(@PathVariable("coordinates") @NotNull final String coordinates) {
        try {
            log.info("Received replay request for [{}]", coordinates);
            final var kafkaMeta = toKafkaMeta(coordinates);

            replayService.replayMessage(kafkaMeta);
            return ResponseEntity.ok(Map.of(
                    "coordinates", coordinates,
                    "parsedKafkaMeta", kafkaMeta,
                    "timestamp", LocalDateTime.now(),
                    "status", "successful"));

        } catch (final UnrecoverableException e) {
            return new ResponseEntity<>(Map.of(
                    "coordinates", coordinates,
                    "status", "failed",
                    "timestamp", LocalDateTime.now(),
                    "exception", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private KafkaMeta toKafkaMeta(final String coordinates) {
        final var matcher = COORDINATES_REGEX.matcher(coordinates);
        if (matcher.matches()) {
            return KafkaMeta.builder()
                    .topic(matcher.group(1))
                    .partition(Integer.parseInt(matcher.group(2)))
                    .offset(Long.parseLong(matcher.group(3)))
                    .build();
        } else {
            throw new UnrecoverableException("Coordinates: " + coordinates + " is not valid");
        }
    }

}
