package se.yolean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.vertx.kafka.client.common.TopicPartition;
import se.yolean.model.Endpoint;
import se.yolean.model.Update;
import se.yolean.model.UpdateInfo;

@ApplicationScoped
public class KeyValueStore {
  
  private HashSet<Endpoint> endpoints = new HashSet<>();
  private Map<String, Update> updateMap = new HashMap<>();
  private Map<TopicPartition, Long> topicPartitionOffset = new HashMap<>();

  // TODO: Is this a hack? Could it cause problems?
  private boolean startupPhase = true;

  public KeyValueStore() {
  }

  public HashSet<Endpoint> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(HashSet<Endpoint> endpoints) {
    this.endpoints = endpoints;
  }

  public Map<String, Update> getUpdateMap() {
    return updateMap;
  }

  public void setUpdateMap(Map<String, Update> updateMap) {
    this.updateMap = updateMap;
  }

  public void addEndpoint(Endpoint endpoint) {
    endpoints.add(endpoint);
  }

  public void removeEndpoint(Endpoint endpoint) {
    endpoints.remove(endpoint);
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

  public boolean isStartupPhase() {
    return startupPhase;
  }

  public void setStartupPhase(boolean startupPhase) {
    this.startupPhase = startupPhase;
  }

  public boolean endpointExists(String ip) {
    return endpoints.stream().filter(o -> o.getIp().equals(ip)).findFirst().isPresent();
  }

  public List<String> getipList() {
    List<String> ipList = new ArrayList<>();
    endpoints.forEach(ep -> ipList.add(ep.getIp()));
    return ipList;
  }

  public void removeEndpointByIp(String ip) {
    endpoints.removeIf(ep -> ep.getIp().equals(ip));
  }
}
