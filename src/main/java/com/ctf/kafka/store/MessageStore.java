package com.ctf.kafka.store;

import com.ctf.kafka.store.model.MessageEntity;
import ctf.avro.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public interface MessageStore extends CrudRepository<MessageEntity, Long> {

    List<MessageEntity> findByPriority(final Long priority);

}


