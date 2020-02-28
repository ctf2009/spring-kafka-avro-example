package com.ctf.kafka.test.support.avro;

import ctf.avro.Message;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;

public class CustomKafkaAvroMessageDeserializer extends KafkaAvroDeserializer {

    public CustomKafkaAvroMessageDeserializer() {
        super();
        this.schemaRegistry = getMockClient(Message.SCHEMA$);
    }

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        return super.deserialize(topic, bytes);
    }

    private static SchemaRegistryClient getMockClient(final Schema schema$) {
        return new MockSchemaRegistryClient() {
            @Override
            public synchronized Schema getById(int id) {
                return schema$;
            }
        };
    }

}
