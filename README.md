# spring-kafka-avro-example
A simple demo of Spring Boot integration with Kafka / Schema Repo / Avro

This project currently demonstrates the following

- Using a @KafkaListener annotation
- Using Spring Kafka Auto Configuration
- Generation of Avro Classes using Gradle
- Testing using Embedded Kafka
- Testing with stubbing a Schema Repository
- Demonstrating the ability to Pause / Resume a Message Listener Container
- Demonstrating the new SeekToCurrentErrorHandler abilities to retry based on specific Exceptions
- Added Example of using ErrorHandlingDeserializer (See Section Below)

## Publishing

You can publish a message using the producer endpoint

`GET /producer/successful` - Produces a simple message with a random UUID

##### Error Demonstration

`GET /error/recoverable` - Produces a simple message which forces a RecoverableException. You will be able to see the message attempted to be retried

`GET /error/recoverable/nested` - Produces a simple message which throws an Exception with the cause being a RecoverableException. You will be able to see the message attmpeted to be retried

`GET /error/unrecoverable` - Produces a simple message which throws an UnrecoverableException. The exception is immediately handled and not retried

## Replay

You can replay a message by using the replay endpoint

`GET /replay/test-topic-0-10` - Attempts to retry processing of offset 10 on partition 0 for the 'test-topic'

## Consumer

You are able to pause, resume and check the pause status using the consumers endpoint

`GET /consumers/pause` - Returns a `202 Accepted` response if the request is successful

`GET /consumers/resume` - Returns a `202 Accepted` response if the request is successful

`GET /consumers/pause/status` - Returns a `200 Ok` response with a map of Container / Status values

**NOTE:** The pause and resume return `202` because it may take some time to fully complete the action

## Kafka Commands

When running with the docker compose, you can run commands like the following to obtain information from kafka

`docker run --net=host confluentinc/cp-server:5.5.3 /usr/bin/kafka-consumer-groups --list --bootstrap-server localhost:9092`

`docker run --net=host confluentinc/cp-server:5.5.3 /usr/bin/kafka-consumer-groups --group testing-group --describe  --bootstrap-server localhost:9092`

`docker run --net=host confluentinc/cp-server:5.5.3 /usr/bin/kafka-topics --list --bootstrap-server localhost:9092`

`docker run --net=host confluentinc/cp-server:5.5.3 /usr/bin/kafka-topics --topic test-topic --describe  --bootstrap-server localhost:9092`

`docker run --net=host confluentinc/cp-server:5.5.3 /usr/bin/kafka-consumer-groups --group testing-group --describe  --bootstrap-server localhost:9092`

`docker run --net=host confluentinc/cp-server:5.5.3 /usr/bin/kafka-consumer-groups --bootstrap-server localhost:9092 --group testing-group --topic test-topic --reset-offsets --to-earliest`

`docker run --net=host confluentinc/cp-server:5.5.3 /usr/bin/kafka-consumer-groups --bootstrap-server localhost:9092 --group testing-group --topic test-topic --reset-offsets --to-earliest --execute`

## ErrorHandlingDeserializer

ErrorHandlingDeserializer is documented [here](https://docs.spring.io/spring-kafka/reference/html/#error-handling-deserializer)

Essentially, if the consumer is unable to deserialize the record, the consumer will loop with an error. The solution is to use the ErrorHandlingDeserializer which delegates to another deserialiser and is able to catch any exceptions thrown by it. In this application, if an exception is thrown during deserialization, the record is immediately sent to the configured ErrorHandler (Although it is possible to perform other actions such as create a default record)

To show the ErrorHandlingDeserializer in action, you can fire an arbitary payload to the the topic and watch the message be sent to the Error Handler. In order to test this, you should first run up the `docker-compose.yaml` in the `testing` directory and then start the application

Once everything is up, run the following command

`docker run --net=host confluentinc/cp-server:5.5.3  sh -c '/bin/echo "Invalid Payload" | /usr/bin/kafka-console-producer kafka-console-producer.sh --broker-list localhost:9092 --topic test-topic'`

You will see that the Error is handled correctly

`
2020-04-23 13:09:17 [org.springframework.kafka.KafkaListenerEndpointContainer#0-0-C-1] WARN c.c.k.c.KafkaListenerConfiguration - Handing Error for record ConsumerRecord ....
`

If the ErrorHandlingDeserializer was not used, the consumer would end up in a loop with an error similar to the following:

```
java.lang.IllegalStateException: This error handler cannot process 'SerializationException's directly; please consider configuring an 'ErrorHandlingDeserializer2' in the value and/or key deserializer
	at org.springframework.kafka.listener.SeekToCurrentErrorHandler.handle(SeekToCurrentErrorHandler.java:187)
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.handleConsumerException(KafkaMessageListenerContainer.java:1149)
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:931)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:830)
Caused by: org.apache.kafka.common.errors.SerializationException: Error deserializing key/value for partition test-topic-0 at offset 2. If needed, please seek past the record to continue consumption.
Caused by: org.apache.kafka.common.errors.SerializationException: Error deserializing Avro message for id -1
Caused by: org.apache.kafka.common.errors.SerializationException: Unknown magic byte!
```

## Schema Registry

##### Basic Auth
The Schema Registry in the `docker-compose.yml` file is set up to use Basic Auth.

In order to populate the password-file correctly you can use the Password Utility to generate the hashed password as shown below

`docker run --net=host confluentinc/cp-schema-registry:5.5.3 /usr/bin/schema-registry-run-class org.eclipse.jetty.util.security.Password user password`

##### Manually Adding Schemas
The Consumer / Producer can be set to not auto register the schema (This is usually something that would be disabled in Prod)
If you choose to disable auto register schema you will need to manually add the schemas to the registry

TODO: Write a Script that can do this from some given inputs

**Set Global Forward Compatibility**
Its normal to do this on a per-subject basis e.g., using the endpoint http://localhost:8081/config/test-topic-value
curl -u user:password -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{"compatibility": "FORWARD"}' http://localhost:8081/config

**Original Schema**
cat message.avsc | jq -c '. | { schema: . | @json }' | curl -u user:password -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data-binary @- http://localhost:8081/subjects/test-topic-value/versions
cat message.avsc | jq -c '. | { schema: . | @json }' | curl -u user:password -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data-binary @- http://localhost:8081/subjects/forwarding-topic-value/versions

**Forward Compatibility evolution of the Schema**
cat message_version2.avsc | jq -c '. | { schema: . | @json }' | curl -u user:password -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data-binary @- http://localhost:8081/subjects/test-topic-value/versions
cat message_version2.avsc | jq -c '. | { schema: . | @json }' | curl -u user:password -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data-binary @- http://localhost:8081/subjects/forwarding-topic-value/versions

