package se.yolean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.vertx.kafka.client.common.TopicPartition;
import se.yolean.model.Update;
import se.yolean.model.UpdateInfo;

@ApplicationScoped
public class KeyValueStore implements Serializable {
  
  private List<String> ipList = new ArrayList<>();
  private Map<String, Update> updateMap = new HashMap<>();
  private Map<TopicPartition, Long> topicPartitionOffset = new HashMap<>();

  public KeyValueStore() {
  }

  public List<String> getIpList() {
    return ipList;
  }

  public void setIpList(List<String> ipList) {
    this.ipList = ipList;
  }

  public Map<String, Update> getUpdateMap() {
    return updateMap;
  }

  public void setUpdateMap(Map<String, Update> updateMap) {
    this.updateMap = updateMap;
  }

  public void addIp(String ip) {
    ipList.add(ip);
  }

  public void removeIp(String ip) {
    ipList.remove(ip);
  }

  public void updateKeyCache(Update update) {
    updateMap.put(update.getKey(), update);
    topicPartitionOffset.put(new TopicPartition(update.getTopic(), update.getPartition()), update.getOffset());
  }

  public Update getKeyCache(String key) {
    return updateMap.get(key);
  }

  public UpdateInfo getUpdateInfo() {
    Map<String, Long> updateInfo = new HashMap<>();
    updateMap.forEach((key, value) -> updateInfo.put(key, value.getOffset()));
    return new UpdateInfo(updateInfo);
  }

  public Long getTopicPartitionOffset(String topic, Integer partition) {
    return topicPartitionOffset.get(new TopicPartition(topic, partition));
  }

  public List<byte[]> getAllValues() {
    List<byte[]> values = new ArrayList<>();
    updateMap.forEach((key, value) -> values.add(value.getValue()));
    return values;
  }
}
