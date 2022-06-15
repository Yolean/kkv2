package se.yolean.kkv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.vertx.kafka.client.common.TopicPartition;
import se.yolean.kkv2.model.Update;
import se.yolean.kkv2.model.UpdateInfo;
import se.yolean.kkv2.model.UpdateTarget;

@ApplicationScoped
public class KeyValueStore {
  
  private HashSet<UpdateTarget> targets = new HashSet<>();
  private Map<String, Update> updateMap = new HashMap<>();
  private Map<TopicPartition, Long> topicPartitionOffset = new HashMap<>();

  public KeyValueStore() {
  }

  public HashSet<UpdateTarget> getTargets() {
    return targets;
  }

  public void setEndpoints(HashSet<UpdateTarget> targets) {
    this.targets = targets;
  }

  public Map<String, Update> getUpdateMap() {
    return updateMap;
  }

  public void setUpdateMap(Map<String, Update> updateMap) {
    this.updateMap = updateMap;
  }

  public void addEndpoint(UpdateTarget target) {
    targets.add(target);
  }

  public void removeEndpoint(UpdateTarget target) {
    targets.remove(target);
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

  public void setTopicPartitionOffset(Map<TopicPartition, Long> topicPartitionOffset) {
    this.topicPartitionOffset = topicPartitionOffset;
  }

  public boolean targetExists(UpdateTarget target) {
    return targets.contains(target);
  }

  public List<String> getipList() {
    List<String> ipList = new ArrayList<>();
    targets.forEach(ep -> ipList.add(ep.getIp()));
    return ipList;
  }

  public void removeEndpointByIp(String ip) {
    targets.removeIf(ep -> ep.getIp().equals(ip));
  }

  public void clearEndpoints() {
    targets.clear();
  }
}
