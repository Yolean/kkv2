package se.yolean.kkv2.http.client;

import java.util.HashSet;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
//import io.vertx.ext.web.client.WebClient;
import se.yolean.kkv2.KeyValueStore;
import se.yolean.kkv2.model.Update;
import se.yolean.kkv2.model.UpdateTarget;

@ApplicationScoped
public class HttpClient {

  @Inject
  KeyValueStore keyValueStore;

  private final Counter failedUpdateDispatchCounter;
  private final Counter successfulUpdateDispatchCounter;

  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
  
  private static Vertx vertx = Vertx.vertx();
      
  private static WebClient client = WebClient.create(vertx);

  private final String endpointPath = "/onupdate";

  @ConfigProperty(name = "kkv.target.service.port")
  int port;

  CircuitBreaker breaker = CircuitBreaker.create("circuit-breaker", vertx,
    new CircuitBreakerOptions()
      .setMaxRetries(2)
      .setMaxFailures(100)
      .setTimeout(4000)
      .setResetTimeout(10000))
      .retryPolicy(retryCount -> retryCount * 500L + (int)(Math.random() * 500L)
  );

  public HttpClient(MeterRegistry meterRegistry) {
    failedUpdateDispatchCounter = meterRegistry.counter("failed_update_dispatch");
    successfulUpdateDispatchCounter = meterRegistry.counter("successful_update_dispatch");
  }

  public void postUpdate(Map<String, Update> newUpdates) {
    HashSet<UpdateTarget> updateTargets = keyValueStore.getTargets();
    JsonObject updateInfo = jsonBuilder(newUpdates);

    for (UpdateTarget target : updateTargets) {
      breaker.execute(future -> {
        client
          .post(port, target.getIp(), endpointPath)
          .sendJsonObject(updateInfo, ar -> {
            if (ar.succeeded() && ar.result().statusCode() == 200) {
              successfulUpdateDispatchCounter.increment();
              logger.debug("Successfully dispatched update to {} on {}:{} with response code {}", target.getName(), target.getIp(), port, ar.result().statusCode());
              future.complete();
            } else {
              failedUpdateDispatchCounter.increment();
              logger.error("Failed to dispatch update for key(s) {} to {} on {}", 
                //newUpdates.keySet() , target.getName(), ar.cause().getMessage());
                newUpdates.keySet(), target.getName(), endpointPath);
              future.fail(ar.cause());
            }
          });
      });
    }
  }

  public void sendCacheNewPod(UpdateTarget target) {
    Map<String, Update> updateMap = keyValueStore.getUpdateMap();
    JsonObject updateInfo = jsonBuilder(updateMap);

    breaker.execute(future -> {
      client
        .post(port, target.getIp(), endpointPath)
        .sendJsonObject(updateInfo, ar -> {
          if (ar.succeeded() && ar.result().statusCode() == 200) {
            successfulUpdateDispatchCounter.increment();
            logger.debug("Successfully dispatched update to {} on {}:{} with response code {}", target.getName(), target.getIp(), port, ar.result().statusCode());
            future.complete();
          } else {
            failedUpdateDispatchCounter.increment();
            logger.error("Failed to dispatch update for key(s) {} to {}:{} on \"/onupdate\" ({})", 
            updateMap.keySet(), target.getName(), ar.cause().getMessage());
            future.fail(ar.cause());
          }
        });
    });
  }

  public JsonObject jsonBuilder(Map<String, Update> updates) {
    JsonObject jsonObject = new JsonObject();

    jsonObject.put("v", 1);
    jsonObject.put("topic", updates.values().iterator().next().getTopic());

    JsonObject offsetsJsonObject = new JsonObject();
    for (Update update : updates.values()) {
      offsetsJsonObject.put(Integer.toString(update.getPartition()), update.getOffset());
    }
    jsonObject.put("offsets", offsetsJsonObject);

    JsonObject updatesJsonObject = new JsonObject();
    for (Update update : updates.values()) {
      updatesJsonObject.put(update.getKey(), new JsonObject());
    }
    jsonObject.put("updates", updatesJsonObject);

    return jsonObject;
  }
}

