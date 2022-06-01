package se.yolean.http.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import se.yolean.model.UpdateInfo;


@ApplicationScoped
public class HttpClient {

  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

  private static Vertx vertx = Vertx.vertx();
  private static WebClient client = WebClient.create(vertx);

  // Todo: Optimize circuit breaker wrt. retries and timeout
  CircuitBreaker initialBreaker = CircuitBreaker.create("onupdate-circuit-breaker", vertx,
    new CircuitBreakerOptions()
      .setMaxRetries(5)
      .setTimeout(2000)
      .setFallbackOnFailure(false)
      .setResetTimeout(1000))
      .retryPolicy(retryCount -> retryCount * 1000L);

  public void postUpdate(UpdateInfo updateInfo, List<String> ipList) {
    // Todo: Perhaps use reason in HashMap to determine cause in Prometheus logging
    Map<String, String> failedIps = new HashMap<>();
    logger.info("Posting update to {}", ipList);
    for (String ip : ipList) {
      initialBreaker.execute(promise -> {
        client
          .post(3000, ip, "/onupdate")
          .sendJson(updateInfo)
          .onFailure(err -> failedIps.put(ip, err.getMessage()));
      });
    }
    // Todo: Have some form of resync to failing on pods in termination stage.
    // Suggestion: Listen to events and delete early in controller or wait longer here and hope that pod is fully terminated during retry period.
    if (!failedIps.isEmpty()) {
      logger.error("Failed to post update to {}", failedIps.keySet());
    }
  }
}
