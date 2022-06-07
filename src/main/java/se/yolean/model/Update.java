package se.yolean.model;

public class Update {
  
  private String topic;
  private int partition;
  private long offset;
  private String key;
  private byte[] value;
  
  public Update(String topic, int partition, long offset, String key, byte[] value) {
    this.topic = topic;
    this.partition = partition;
    this.offset = offset;
    this.key = key;
    this.value = value;
  }

  public Update() {
  }
  
  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public int getPartition() {
    return partition;
  }

  public void setPartition(int partition) {
    this.partition = partition;
  }
  
  public long getOffset() {
    return offset;
  }
  
  public String getKey() {
    return key;
  }
  
  public byte[] getValue() {
    return value;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Update [topic=" + topic + ", partition=" + partition + ", offset=" + offset + ", key=" + key + ", value="
        + value + "]";
  }

}