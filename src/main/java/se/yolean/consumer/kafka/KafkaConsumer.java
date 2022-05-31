package se.yolean.consumer.kafka;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.mutiny.core.eventbus.EventBus;

import se.yolean.model.Update;
import se.yolean.model.UpdateInfo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

@ApplicationScoped
public class KafkaConsumer {

  private Map<String, Update> updateMap = new HashMap<>();

  final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

  @Inject
  EventBus bus;

  public Update getTopicUpdate(String key) {
    return updateMap.get(key);
  }

  private void updateKeyCache(Update update) {
    updateMap.put(update.getKey(), update);
  }
  
  @Incoming("config")
  public void consumer(ConsumerRecords<String, String> records) {
    logger.info("New record(s) received");
    for (ConsumerRecord<String, String> record : records) {
      if (record.key() != null) {
        TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
        Update update = new Update(topicPartition, record.offset(), record.key(), record.value());
        updateKeyCache(update);
      }
    }
    sendUpdateEvent();
  }

  public void sendUpdateEvent() {
    Map<String, Long> updateInfo = new HashMap<>();
    updateMap.forEach((key, update) -> updateInfo.put(key, update.getOffset()));
    UpdateInfo updateInfoObject = new UpdateInfo(updateInfo);
    bus.publish("newConfigEvent", updateInfoObject);


  }
}
