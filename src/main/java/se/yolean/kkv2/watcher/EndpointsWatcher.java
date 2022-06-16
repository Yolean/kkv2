package se.yolean.kkv2.watcher;

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import se.yolean.kkv2.KeyValueStore;
import se.yolean.kkv2.http.client.HttpClient;
import se.yolean.kkv2.model.UpdateTarget;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class EndpointsWatcher implements QuarkusApplication {

  @Inject
  KubernetesClient client;
  @Inject
  HttpClient httpClient;

  private final KeyValueStore keyValueStore;

  private static final Logger logger = LoggerFactory.getLogger(EndpointsWatcher.class);

  @ConfigProperty(name = "kkv.target.service.name")
  String serviceName;

  public EndpointsWatcher(MeterRegistry meterRegistry, KeyValueStore keyValueStore) {
    this.keyValueStore = keyValueStore;
    meterRegistry.gaugeCollectionSize("number_of_endpoints", Tags.empty(), keyValueStore.getTargets());
    meterRegistry.gaugeCollectionSize("number_of_keys", Tags.empty(), keyValueStore.getUpdateMap().keySet());

  }

  @Override
  public int run(String... args) throws Exception {
    client.endpoints().withName(serviceName).watch(new Watcher<Endpoints>() {
      @Override
      public void eventReceived(Action action, Endpoints resource) {
        if (action == Action.ADDED) {
          keyValueStore.clearUpdateTargets();
          resource.getSubsets().stream()
            .map(subset -> subset.getAddresses())
            .flatMap(Collection::stream)
            .forEach(target -> keyValueStore.addUpdateTarget(new UpdateTarget(target.getTargetRef().getName(), target.getIp())));
          
          logger.info("Initial targets: {}", keyValueStore.getTargets().toString());
        }

        else if (action == Action.MODIFIED) {
          List<String> oldTargets = keyValueStore.getipList();
          keyValueStore.clearUpdateTargets();
          resource.getSubsets().stream()
            .map(slice -> slice.getAddresses())
            .flatMap(Collection::stream)
            .forEach(target -> keyValueStore.addUpdateTarget(new UpdateTarget(target.getTargetRef().getName(), target.getIp())));

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
    Quarkus.run(EndpointsWatcher.class, args);
  }
}