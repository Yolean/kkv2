package se.yolean.model;

import java.util.Map;

public class UpdateInfo {
  
  private Map<String, Long> updateInfo;

  public UpdateInfo(Map<String, Long> updateInfo) {
    this.updateInfo = updateInfo;
  }

  public UpdateInfo() {
  }

  public Map<String, Long> getUpdateInfo() {
    return updateInfo;
  }

  public void setUpdateInfo(Map<String, Long> updateInfo) {
    this.updateInfo = updateInfo;
  }

}
