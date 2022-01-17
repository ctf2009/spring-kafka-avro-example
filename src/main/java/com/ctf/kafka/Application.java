package com.ctf.kafka;

import ctf.avro.Test;
import ctf.avro.TestOuter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.time.Instant;

@Slf4j
@SpringBootApplication
public class Application {

	@Autowired
	private KafkaTemplate<String, TestOuter> kafkaTemplate;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void postConstruct() {
		log.info("In Post Construct");


		final TestOuter outer = TestOuter.newBuilder()
				.setName("Hello")
				.setTest(Test.newBuilder()
						.setName("Hello")
						.setDate(Instant.parse("2021-10-15T09:16:56.517499Z"))
						.build())
				.build();

		var future = kafkaTemplate.send("rubbish-topic-outer-again", outer);
		future.addCallback(new ListenableFutureCallback<SendResult<String, TestOuter>>() {
			@Override
			public void onSuccess(SendResult<String, TestOuter> result) {
				log.info("Message Success: {}", result);
			}

			@Override
			public void onFailure(Throwable ex) {
				log.error("Failed: ", ex);
			}
		});

	}

}
