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
  
  private HashSet<UpdateTarget> updateTargets = new HashSet<>();
  private Map<String, Update> updateMap = new HashMap<>();
  private Map<TopicPartition, Long> topicPartitionOffset = new HashMap<>();

  public KeyValueStore() {
  }

  public HashSet<UpdateTarget> getTargets() {
    return updateTargets;
  }

  public void setUpdateTargets(HashSet<UpdateTarget> updateTargets) {
    this.updateTargets = updateTargets;
  }

  public Map<String, Update> getUpdateMap() {
    return updateMap;
  }

  public void setUpdateMap(Map<String, Update> updateMap) {
    this.updateMap = updateMap;
  }

  public void addUpdateTarget(UpdateTarget target) {
    updateTargets.add(target);
  }

  public void removeUpdateTarget(UpdateTarget target) {
    updateTargets.remove(target);
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

  public boolean updateTargetExists(UpdateTarget updateTarget) {
    return updateTargets.contains(updateTarget);
  }

  public List<String> getipList() {
    List<String> ipList = new ArrayList<>();
    updateTargets.forEach(ep -> ipList.add(ep.getIp()));
    return ipList;
  }

  public void removeUpdateTargetByIp(String ip) {
    updateTargets.removeIf(ep -> ep.getIp().equals(ip));
  }

  public void clearUpdateTargets() {
    updateTargets.clear();
  }
}
