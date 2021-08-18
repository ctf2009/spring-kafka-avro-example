package com.ctf.kafka.service;

import org.springframework.kafka.support.Acknowledgment;

public class NoopAcknowledgement implements Acknowledgment {
    @Override
    public void acknowledge() {}

    @Override
    public void nack(long sleep) {}

    @Override
    public void nack(int index, long sleep) {}
}
