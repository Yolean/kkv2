package se.yolean.http.server;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import se.yolean.KeyValueStore;
import se.yolean.model.Update;

@Path("/config")
public class ConfigResource {

  @Inject
  KeyValueStore keyValueStore;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{key}")
  public Update greeting(String key) {
    return keyValueStore.getKeyCache(key);
  }

}
