package com.ctf.kafka.store.model;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @Column(name = "KAFKA_TOPIC")
    private String topic;

    @Column(name = "KAFKA_PARTITION")
    private Integer partition;

    @Column(name = "KAFKA_OFFSET")
    private Long offset;

}
