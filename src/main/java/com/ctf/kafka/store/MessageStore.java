package com.ctf.kafka.store;

import com.ctf.kafka.store.model.MessageEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageStore extends CrudRepository<MessageEntity, Long> {

    List<MessageEntity> findByPriority(final Long priority);

}


