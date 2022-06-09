package se.yolean.controller.kubernetes;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;

import io.vertx.core.eventbus.EventBus;
import se.yolean.KeyValueStore;
import se.yolean.http.client.HttpClient;
import se.yolean.model.Endpoint;

@ApplicationScoped
public class KubernetesControllerApplicationConfig {

  final Logger logger = LoggerFactory.getLogger(KubernetesControllerApplicationConfig.class);

  @Inject
  KubernetesClient client;

  @Inject
  HttpClient httpClient;

  @Inject
  KeyValueStore keyValueStore;

  @Inject
  EventBus bus;

  String TARGET_LABEL = "kkv-test-client";

  @Singleton
  SharedInformerFactory sharedInformerFactory() {
    return client.informers();
  }

  @Singleton
  SharedIndexInformer<Pod> podInformer(SharedInformerFactory factory) {
    return factory.sharedIndexInformerFor(Pod.class, 60 * 1000);
  }

  // Include readiness somehow in order to avoid post to unavailable pods
  // Idea: When Pod is no longer ready, save timestamp och compare to latest record to determine if push is needed when ready.

  // TODO: Differentiate between startup and new pod after startup so that we do not push updates two times. should be solved with phase boolean.
  @Singleton
  ResourceEventHandler<Pod> podReconciler(SharedIndexInformer<Pod> podInformer) {
    return new ResourceEventHandler<>() {

      @Override
      public void onAdd(Pod pod) {
        String podIp = pod.getStatus().getPodIP();
        if (podIp != null && pod.getMetadata().getLabels().containsValue(TARGET_LABEL)) {
          if (!keyValueStore.endpointExists(podIp)) {
            keyValueStore.addEndpoint(new Endpoint(pod.getMetadata().getName(), podIp));
            if(!keyValueStore.isStartupPhase()) {
              httpClient.sendCacheNewPod(podIp);
            } 
          }
        }
      }

      @Override
      public void onUpdate(Pod oldPod, Pod newPod) {
        String oldIp = oldPod.getStatus().getPodIP();
        String newIp = newPod.getStatus().getPodIP();

        // https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/

        // Could be used to get ports if we can label the correct container for each pod.
        /* newPod.getSpec().getContainers().stream().forEach(c -> {
          c.getPorts());
        }); */


        // Could be used to determine if pod's containers are ready and thus if pod is ready.
        /* newPod.getStatus().getContainerStatuses().stream().forEach(c -> {
          c.getReady();
        }); */
          

        if(newPod.isMarkedForDeletion() && keyValueStore.endpointExists(newIp)) {
          keyValueStore.removeEndpointByIp(newIp);
        }
        else if (newPod.getMetadata().getLabels().containsValue(TARGET_LABEL)) {
          if (oldIp == null && newIp != null && !keyValueStore.endpointExists(newIp)) {
            keyValueStore.addEndpoint(new Endpoint(newPod.getMetadata().getName(), newIp));
            if(!keyValueStore.isStartupPhase()) {
              httpClient.sendCacheNewPod(newIp);
            }
          }
        }
      }

      @Override
      public void onDelete(Pod pod, boolean deletedFinalStateUnknown) {
        String podIp = pod.getStatus().getPodIP();
        if(podIp != null && pod.getMetadata().getLabels().containsValue(TARGET_LABEL)) {
          keyValueStore.removeEndpointByIp(podIp);
        }
      }
    };
  }
}
