package se.yolean;

import java.util.List;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import se.yolean.http.client.HttpClient;
import se.yolean.model.Update;

@QuarkusTest
public class JsonBuilderTest {

  HttpClient httpClient = new HttpClient();

  @Test
  public void assertCorrectOnUpdateJsonBodyStructure() {
    List<Update> updateList = List.of(new Update("test-topic", 0, 0, "key1", "value1".getBytes()));

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
