package se.yolean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import se.yolean.model.Update;

@Singleton
public class KeyValueStore {
  
  private static List<String> ipList = new ArrayList<>();
  private static Map<String, Update> updateMap = new HashMap<>();

  public KeyValueStore() {
  }

  public List<String> getIpList() {
    return ipList;
  }

  public void setIpList(List<String> ipList) {
    KeyValueStore.ipList = ipList;
  }

  public Map<String, Update> getUpdateMap() {
    return updateMap;
  }

  public void setUpdateMap(Map<String, Update> updateMap) {
    KeyValueStore.updateMap = updateMap;
  }

  public void addIp(String ip) {
    ipList.add(ip);
  }

  public void removeIp(String ip) {
    ipList.remove(ip);
  }

  public void updateKeyCache(Update update) {
    updateMap.put(update.getKey(), update);
  }

  public Update getKeyCache(String key) {
    return updateMap.get(key);
  }
}
