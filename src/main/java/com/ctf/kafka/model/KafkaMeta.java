package com.ctf.kafka.model;

import lombok.Builder;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Data
@Builder
public class KafkaMeta {

    final String topic;
    final int partition;
    final long offset;

    public static KafkaMeta fromConsumerRecord(final ConsumerRecord<?, ?> consumerRecord) {
        return KafkaMeta.builder()
                .topic(consumerRecord.topic())
                .partition(consumerRecord.partition())
                .offset(consumerRecord.offset())
                .build();
    }
}
