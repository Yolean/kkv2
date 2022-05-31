package se.yolean.model;

import io.vertx.kafka.client.common.TopicPartition;

public class Update {
  
  private TopicPartition topicPartition;
  private long offset;
  private String key;
  private String value;
  
  public Update(TopicPartition topicPartition, long offset, String key, String value) {
    this.topicPartition = topicPartition;
    this.offset = offset;
    this.key = key;
    this.value = value;
  }

  public Update() {
  }
  
  public TopicPartition getTopicPartition() {
    return topicPartition;
  }
  
  public long getOffset() {
    return offset;
  }
  
  public String getKey() {
    return key;
  }
  
  public String getValue() {
    return value;
  }

  public void setTopicPartition(TopicPartition topicPartition) {
    this.topicPartition = topicPartition;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Update [topicPartition=" + topicPartition + ", offset=" + offset + ", key=" + key + ", value=" + value + "]";
  }

}