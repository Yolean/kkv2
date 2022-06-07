package se.yolean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import se.yolean.consumer.kafka.KafkaConsumer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;



@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource.class)
public class KafkaConsumerTest {
  
  @InjectKafkaCompanion
  KafkaCompanion companion;

  @Inject
  KeyValueStore keyValueStore;

  @Inject
  KafkaConsumer kafkaConsumer;

  @Test
  public void test() {
    Map<TopicPartition, List<ConsumerRecord<String, byte[]>>> records = new LinkedHashMap<>();

    String topic = "topic";
    records.put(new TopicPartition(topic, 0), new ArrayList<ConsumerRecord<String, byte[]>>());
    ConsumerRecord<String, byte[]> record1 = new ConsumerRecord<String,byte[]>(topic, 1, 0, "key", "value1".getBytes());
    ConsumerRecord<String, byte[]> record2 = new ConsumerRecord<String,byte[]>(topic, 1, 0, "key", "value2".getBytes());
    records.put(new TopicPartition(topic, 1), Arrays.asList(record1, record2));
    records.put(new TopicPartition(topic, 2), new ArrayList<ConsumerRecord<String, byte[]>>());
    ConsumerRecords<String, byte[]> consumerRecords = new ConsumerRecords<>(records);

    kafkaConsumer.consumer(consumerRecords);

    given()
      .when().get("/cache/v1/raw/key")
      .then()
        .statusCode(200)
        .body(is("value2"));
    

    
  }
}
