# spring-kafka-avro-example
A simple demo of Spring Boot integration with Kafka / Schema Repo / Avro

This project currently demonstrates the following

- Using a @KafkaListener annotation
- Using Spring Kafka Auto Configuration
- Generation of Avro Classes using Gradle
- Testing using Embedded Kafka
- Testing with stubbing a Schema Repository
- Demonstrating the ability to Pause / Resume a Message Listener Container

The following are in progress:

- Error Handling using the new abilities of the SeekToCurrentErrorHandler
- Switching to Manual Acknowledgements and integrating this with the Error Handler

### Publishing

You can publish a message using the producer endpoint

`GET /producer/publish` - Produces a simple message with a random UUID

### Consumer

You are able to pause, resume and check the pause status using the consumers endpoint

`GET /consumers/pause` - Returns a `202 Accepted` response if the request is successful

`GET /consumers/resume` - Returns a `202 Accepted` response if the request is successful

`GET /consumers/pause/status` - Returns a `200 Ok` response with a map of Container / Status values

**NOTE:** The pause and resume return `202` because it may take some time to fully complete the action

### Kafka Commands

When running with the docker compose, you can run commands like the following to obtain information from kafka

```
docker run --net=host confluentinc/cp-server:5.4.0 /usr/bin/kafka-consumer-groups --list --bootstrap-server localhost:9092
```

```
docker run --net=host confluentinc/cp-server:5.4.0 /usr/bin/kafka-consumer-groups --group testing-group --describe  --bootstrap-server localhost:9092
```

```
docker run --net=host confluentinc/cp-server:5.4.0 /usr/bin/kafka-topics --list --bootstrap-server localhost:9092
```

```
docker run --net=host confluentinc/cp-server:5.4.0 /usr/bin/kafka-topics --topic test-topic --describe  --bootstrap-server localhost:9092
```

