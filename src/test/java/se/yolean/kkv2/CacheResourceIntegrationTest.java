package se.yolean.kkv2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import se.yolean.kkv2.consumer.kafka.KafkaConsumer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class CacheResourceIntegrationTest {

  @Inject
  KafkaConsumer kafkaConsumer;

  static ConsumerRecords<String, byte[]> consumerRecords;

  @BeforeAll
  public static void setup() {
    String topic = "topic";
    Map<TopicPartition, List<ConsumerRecord<String, byte[]>>> records = new LinkedHashMap<>();
    records.put(new TopicPartition(topic, 0), new ArrayList<ConsumerRecord<String, byte[]>>());
    ConsumerRecord<String, byte[]> record1 = new ConsumerRecord<String,byte[]>(topic, 1, 0, "key1", "value1".getBytes());
    ConsumerRecord<String, byte[]> record2 = new ConsumerRecord<String,byte[]>(topic, 1, 1, "key1", "value2".getBytes());
    ConsumerRecord<String, byte[]> record3 = new ConsumerRecord<String,byte[]>(topic, 1, 2, "key2", "value3".getBytes());
    records.put(new TopicPartition(topic, 1), Arrays.asList(record1, record2, record3));
    consumerRecords = new ConsumerRecords<>(records);
  }

  @Test
  public void assertThatLatestRecordReturnedForKey() {
    kafkaConsumer.consumer(consumerRecords);

    given()
      .when().get("/cache/v1/raw/key1")
      .then()
        .statusCode(200)
        .body(is("value2"));

    given()
    .when().get("/cache/v1/raw/key2")
    .then()
      .statusCode(200)
      .body(is("value3"));
  }

  @Test
  public void assertCorrectOffset() {
    kafkaConsumer.consumer(consumerRecords);

    given()
      .when().get("/cache/v1/offset/topic/1")
      .then()
        .statusCode(200)
        .body(is("2"));
  }
}
