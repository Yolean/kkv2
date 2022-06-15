package se.yolean.kkv2.model;

public class UpdateTarget {
  
  private String name;
  private String ip;

  public UpdateTarget(String name, String ip) {
    this.name = name;
    this.ip = ip;
  }

  public UpdateTarget() {
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getIp() {
    return ip;
  }

  @Override
  public String toString() {
    return "Endpoint [name=" + name + ", ip=" + ip + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ip == null) ? 0 : ip.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UpdateTarget other = (UpdateTarget) obj;
    if (ip == null) {
      if (other.ip != null)
        return false;
    } else if (!ip.equals(other.ip))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

}
