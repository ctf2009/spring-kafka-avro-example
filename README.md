# spring-kafka-avro-example
A simple demo of Spring Boot integration with Kafka / Schema Repo / Avro

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

