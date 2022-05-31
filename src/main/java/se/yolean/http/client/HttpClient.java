package se.yolean.http.client;

import java.util.List;

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

  CircuitBreaker breaker = CircuitBreaker.create("onupdate-circuit-breaker", vertx,
    new CircuitBreakerOptions()
      .setMaxFailures(8)
      .setTimeout(2000)
      .setFallbackOnFailure(false)
      .setResetTimeout(1000));

  public void postUpdate(UpdateInfo updateInfo, List<String> ipList) {
    logger.info("Posting new config event: " + updateInfo.toString());
    logger.info("Sending update to " + ipList.size() + " client(s)");
    for (String ip : ipList) {
        logger.info("Sending update to {}", ip);  
        client
          .post(3000, ip, "/onupdate")
          .sendJson(updateInfo)
          .onSuccess(response -> System.out.println("Received response with status code" + response.statusCode() ))
          .onFailure(err -> System.out.println("Something went wrong " + err.getMessage()));
    }
  }
}
