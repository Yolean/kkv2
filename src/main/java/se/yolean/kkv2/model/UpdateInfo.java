package se.yolean.kkv2.model;

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

  @Override
  public String toString() {
    return "UpdateInfo [updateInfo=" + updateInfo + "]";
  }
}
