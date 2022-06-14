package se.yolean.watcher;

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import se.yolean.KeyValueStore;
import se.yolean.http.client.HttpClient;
import se.yolean.model.UpdateTarget;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class EndpointSliceWatcher implements QuarkusApplication {

  @Inject
  KubernetesClient client;
  @Inject
  KeyValueStore keyValueStore;
  @Inject
  HttpClient httpClient;

  private static final Logger logger = LoggerFactory.getLogger(EndpointSliceWatcher.class);

  @ConfigProperty(name = "kkv.target.service.name")
  String serviceName;

  @Override
  public int run(String... args) throws Exception {

    client.endpoints().withName(serviceName).watch(new Watcher<Endpoints>() {
      @Override
      public void eventReceived(Action action, Endpoints resource) {
        if (action == Action.ADDED) {
          keyValueStore.clearEndpoints();
          resource.getSubsets().stream()
            .map(subset -> subset.getAddresses())
            .flatMap(Collection::stream)
            .forEach(target -> keyValueStore.addEndpoint(new UpdateTarget(target.getTargetRef().getName(), target.getIp())));            
          logger.info("Initial targets: {}", keyValueStore.getTargets().toString());
        }

        else if (action == Action.MODIFIED) {
          List<String> oldTargets = keyValueStore.getipList();
          keyValueStore.clearEndpoints();
          resource.getSubsets().stream()
            .map(slice -> slice.getAddresses())
            .flatMap(Collection::stream)
            .forEach(target -> keyValueStore.addEndpoint(new UpdateTarget(target.getTargetRef().getName(), target.getIp())));

          keyValueStore.getTargets().stream()
            .filter(target -> !oldTargets.contains(target.getIp()))
            .forEach(target -> httpClient.sendCacheNewPod(target));
          logger.info("Targets: {}", keyValueStore.getTargets().toString());
        }
      }

      @Override
      public void onClose(WatcherException cause) {
        logger.info(cause.getMessage());
      }
    });
    
    Quarkus.waitForExit();
    return 0;
  }

  public static void main(String... args) {
    Quarkus.run(EndpointSliceWatcher.class, args);
  }
}