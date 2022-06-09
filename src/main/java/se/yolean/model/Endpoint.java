package se.yolean.model;

public class Endpoint {
  
  private String name;
  private String ip;

  public Endpoint(String name, String ip) {
    this.name = name;
    this.ip = ip;
  }

  public Endpoint() {
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


}
