package se.yolean.http.client;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import se.yolean.KeyValueStore;
import se.yolean.model.Update;

//@ApplicationScoped
@Singleton
public class HttpClient {

  @Inject
  KeyValueStore keyValueStore;

  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

  private static Vertx vertx = Vertx.vertx();
  private static WebClient client = WebClient.create(vertx);

  public void postUpdate(List<Update> updateList) {
    List<String> ipList = keyValueStore.getIpList();
    JsonObject updateInfo = jsonBuilder(updateList);

    for (String ip : ipList) {
      client
      .post(3000, ip, "/onupdate")
      .sendJsonObject(updateInfo, ar -> {
        if (ar.succeeded()) {
          logger.info("Successfully posted update to " + ip);
        } else {
          logger.error("Failed to post update to " + ip);
        }
      });
    }
  }

  public JsonObject jsonBuilder(List<Update> updates) {
    JsonObject jsonObject = new JsonObject();

    jsonObject.put("v", 1);
    // TODO: FIX THIS, THIS IS A HACK FOR NOW. How should we handle multiple topics?
    jsonObject.put("topic", updates.get(0).getTopic());

    JsonObject offsetsJsonObject = new JsonObject();
    for (Update update : updates) {
      offsetsJsonObject.put(Integer.toString(update.getPartition()), update.getOffset());
    }
    jsonObject.put("offsets", offsetsJsonObject);

    JsonObject updatesJsonObject = new JsonObject();
    for (Update update : updates) {
      updatesJsonObject.put(update.getKey(), new JsonObject());
    }
    jsonObject.put("updates", updatesJsonObject);

    return jsonObject;
  }
}

