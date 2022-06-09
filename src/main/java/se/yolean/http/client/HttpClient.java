package se.yolean.http.client;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import se.yolean.KeyValueStore;
import se.yolean.model.Update;

@ApplicationScoped
public class HttpClient {

  @Inject
  KeyValueStore keyValueStore;

  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

  private static Vertx vertx = Vertx.vertx();
  private static WebClient client = WebClient.create(vertx);

  CircuitBreaker breaker = CircuitBreaker.create("circuit-breaker", vertx,
  new CircuitBreakerOptions().setMaxRetries(5).setTimeout(2000));

  public void postUpdate(List<Update> updateList) {
    List<String> ipList = keyValueStore.getIpList();
    JsonObject updateInfo = jsonBuilder(updateList);

    for (String ip : ipList) {
      breaker.execute(future -> {
        client
          .post(3000, ip, "/onupdate")
          .sendJsonObject(updateInfo, ar -> {
        if (ar.succeeded()) {
          future.complete();
        } else {
          logger.info("FAILED");
          future.fail(ar.cause());
        }
      });
      });
    }
  }

  public void sendCacheNewPod(String ip) {
    List<Update> updateList = new ArrayList<>(keyValueStore.getUpdateMap().values());
    JsonObject updateInfo = jsonBuilder(updateList);

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

