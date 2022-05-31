package se.yolean.controller.kubernetes;

import io.fabric8.kubernetes.api.model.ListOptionsBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

@QuarkusMain
public class KubernetesControllerApplication implements QuarkusApplication {

  @Inject
  KubernetesClient client;
  @Inject
  SharedInformerFactory sharedInformerFactory;
  @Inject
  ResourceEventHandler<Pod> podEventHandler;

  @Override
  public int run(String... args) throws Exception {
    try {
      client.pods().list(new ListOptionsBuilder().withLimit(1L).build());
    } catch (KubernetesClientException ex) {
      System.out.println(ex.getMessage());
      return 1;
    }
    sharedInformerFactory.startAllRegisteredInformers().get();
    final var podHandler = sharedInformerFactory.getExistingSharedIndexInformer(Pod.class);
    podHandler.addEventHandler(podEventHandler);
    Quarkus.waitForExit();
    return 0;
  }

  void onShutDown(@Observes ShutdownEvent event) {
    sharedInformerFactory.stopAllRegisteredInformers(true);
  }

  public static void main(String... args) {
    Quarkus.run(KubernetesControllerApplication.class, args);
  }
}