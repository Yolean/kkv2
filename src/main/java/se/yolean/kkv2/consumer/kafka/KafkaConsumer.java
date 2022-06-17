package se.yolean.kkv2.consumer.kafka;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import se.yolean.kkv2.KeyValueStore;
import se.yolean.kkv2.http.client.HttpClient;
import se.yolean.kkv2.model.Update;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

@ApplicationScoped
public class KafkaConsumer {

  private final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

  private final Counter recordsConsumedCounter;

  @Inject
  HttpClient httpClient;

  KeyValueStore keyValueStore;

  public KafkaConsumer(KeyValueStore keyValueStore, MeterRegistry meterRegistry) {
    this.keyValueStore = keyValueStore;
    recordsConsumedCounter = meterRegistry.counter("records_consumed");
  }

  @Incoming("config")
  public void consumer(ConsumerRecords<String, byte[]> records) {
    logger.info("Consuming new record(s)");
    Map<String, Update> newUpdates = new HashMap<>();
    for (ConsumerRecord<String, byte[]> record : records) {
      if (record.key() != null) {
        recordsConsumedCounter.increment();
        Update update = new Update(record.topic(), record.partition(), record.offset(), record.key(), record.value());
        keyValueStore.updateKeyCache(update);
        newUpdates.put(update.getKey(), update);
        logger.debug("Consumed new record on topic: {} with key: {} and offset: {}", record.topic(), record.key(), record.offset());
      }
    }
    httpClient.postUpdate(newUpdates);
  }
}
