package se.yolean.consumer.kafka;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.mutiny.core.eventbus.EventBus;
import se.yolean.KeyValueStore;
import se.yolean.http.client.HttpClient;
import se.yolean.model.Update;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

@ApplicationScoped
public class KafkaConsumer {

  private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

  @Inject
  EventBus bus;

  @Inject
  KeyValueStore keyValueStore;

  @Inject
  HttpClient httpClient;
  
  @Incoming("config")
  public void consumer(ConsumerRecords<String, byte[]> records) {
    logger.info("New config @incoming");
    List<Update> updateList = new ArrayList<>();
    for (ConsumerRecord<String, byte[]> record : records) {
      if (record.key() != null) {
        Update update = new Update(record.topic(), record.partition(), record.offset(), record.key(), record.value());
        keyValueStore.updateKeyCache(update);
        updateList.add(update);
      }
    }
    //httpClient.postUpdate(keyValueStore.getUpdateInfo(), keyValueStore.getIpList());

    httpClient.postUpdate(updateList);
    
  }
}
