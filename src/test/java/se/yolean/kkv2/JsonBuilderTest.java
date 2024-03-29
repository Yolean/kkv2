package se.yolean.kkv2;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import se.yolean.kkv2.http.client.HttpClient;
import se.yolean.kkv2.model.Update;

@QuarkusTest
public class JsonBuilderTest {

  HttpClient httpClient = new HttpClient(new SimpleMeterRegistry());

  @Test
  public void assertCorrectOnUpdateJsonBodyStructure() {
    Map<String, Update> updateList = Map.of("key1", new Update("test-topic", 0, 0, "key1", "value1".getBytes()));

    JsonObject jsonObject = httpClient.jsonBuilder(updateList);

    Assertions.assertEquals(
      "{" +
        "\"v\":1," +
        "\"topic\":\"test-topic\"," +
        "\"offsets\":{" +
          "\"0\":0" +
        "}," +
        "\"updates\":{" +
          "\"key1\":{}" +
        "}" +
      "}", jsonObject.toString());
  }
}
