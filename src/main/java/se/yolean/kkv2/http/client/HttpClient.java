package se.yolean.kkv2.http.client;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import se.yolean.kkv2.KeyValueStore;
import se.yolean.kkv2.model.Update;
import se.yolean.kkv2.model.UpdateTarget;

@ApplicationScoped
public class HttpClient {

  @Inject
  KeyValueStore keyValueStore;

  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

  private static Vertx vertx = Vertx.vertx();
  private static WebClient client = WebClient.create(vertx);

  @ConfigProperty(name = "kkv.target.service.port")
  int port;

  CircuitBreaker breaker = CircuitBreaker.create("circuit-breaker", vertx,
  new CircuitBreakerOptions().setMaxRetries(5).setTimeout(2000));

  public void postUpdate(List<Update> updateList) {
    List<String> ipList = keyValueStore.getipList();
    JsonObject updateInfo = jsonBuilder(updateList);

    for (String ip : ipList) {
      breaker.execute(future -> {
        client
          .post(port, ip, "/onupdate")
          .sendJsonObject(updateInfo, ar -> {
        if (ar.succeeded()) {
          future.complete();
          logger.debug("Successfully dispatched update to ip: {}:{}", ip, port);
        } else {
          logger.error("Failed to dispatch update to ip {}:{}", ip, port);
          future.fail(ar.cause());
        }
        });
      });
    }
  }

  public void sendCacheNewPod(UpdateTarget endpoint) {
    String ip = endpoint.getIp();
    List<Update> updateList = new ArrayList<>(keyValueStore.getUpdateMap().values());
    JsonObject updateInfo = jsonBuilder(updateList);

    breaker.execute(future -> {
      client
        .post(port, ip, "/onupdate")
        .sendJsonObject(updateInfo, ar -> {
      if (ar.succeeded()) {
        future.complete();
        logger.debug("Successfully dispatched update to ip: {}:{}", ip, port);
      } else {
        logger.error("Failed to dispatch update to ip {}:{}", ip, port);
        future.fail(ar.cause());
      }
      });
    });
  }

  public JsonObject jsonBuilder(List<Update> updates) {
    JsonObject jsonObject = new JsonObject();

    jsonObject.put("v", 1);
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

