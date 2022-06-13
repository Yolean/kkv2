package se.yolean.watcher;

import io.fabric8.kubernetes.api.model.discovery.v1beta1.EndpointSlice;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import se.yolean.KeyValueStore;
import se.yolean.http.client.HttpClient;
import se.yolean.model.UpdateTarget;

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

  @ConfigProperty(name = "kkv.namespace")
  String namespace;

  @ConfigProperty(name = "kkv.endpointslice.name")
  String endpointsliceName;

  @Override
  public int run(String... args) throws Exception {

    client.discovery().v1beta1().endpointSlices().inNamespace(namespace).withName(endpointsliceName).watch(new Watcher<EndpointSlice>() {
      @Override
      public void eventReceived(Action action, EndpointSlice resource) {
        if (action == Action.ADDED) {
          keyValueStore.clearEndpoints();
          resource.getEndpoints().forEach(endpoint -> {
            keyValueStore.addEndpoint(new UpdateTarget(endpoint.getTargetRef().getName(), endpoint.getAddresses().get(0)));
          });
          logger.info("Initial targets: {}", keyValueStore.getTargets().toString());
          keyValueStore.setStartupPhase(false);
        }

        else if (action == Action.MODIFIED) {
          List<String> oldTargets = keyValueStore.getipList();
          keyValueStore.clearEndpoints();
          resource.getEndpoints().stream()
            .filter(endpoint -> endpoint.getConditions().getReady())
            .forEach(endpoint -> {
            keyValueStore.addEndpoint(new UpdateTarget(endpoint.getTargetRef().getName(), endpoint.getAddresses().get(0)));
          });

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