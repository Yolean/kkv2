package se.yolean.controller.kubernetes;

import java.util.ArrayList;
import java.util.List;

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
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import se.yolean.http.client.HttpClient;
import se.yolean.model.UpdateInfo;

@ApplicationScoped
public class KubernetesControllerApplicationConfig {

  final Logger logger = LoggerFactory.getLogger(KubernetesControllerApplicationConfig.class);

  @Inject
  KubernetesClient client;

  @Inject
  HttpClient httpClient;

  @Inject
  EventBus bus;

  List<String> ipList = new ArrayList<>();

  String TARGET_LABEL = "kkv-test-client";

  @Singleton
  SharedInformerFactory sharedInformerFactory() {
    return client.informers();
  }

  @Singleton
  SharedIndexInformer<Pod> podInformer(SharedInformerFactory factory) {
    return factory.sharedIndexInformerFor(Pod.class, 60 * 1000);
  }

  @Singleton
  ResourceEventHandler<Pod> podReconciler(SharedIndexInformer<Pod> podInformer) {
    return new ResourceEventHandler<>() {

      @Override
      public void onAdd(Pod pod) {
        String podIp = pod.getStatus().getPodIP();

        if (podIp != null && pod.getMetadata().getLabels().containsValue(TARGET_LABEL)) {
          if (!ipList.contains(podIp)) {
            ipList.add(podIp);
            bus.publish("newSubscriber", podIp);
          }
        }
      }

      @Override
      public void onUpdate(Pod oldPod, Pod newPod) {
        String oldIp = oldPod.getStatus().getPodIP();
        String newIp = newPod.getStatus().getPodIP();

        if (newPod.getMetadata().getLabels().containsValue(TARGET_LABEL)) {
          if (oldIp == null && newIp != null && !ipList.contains(newIp)) {
            ipList.add(newIp);
            bus.publish("newSubscriber", newIp);
          }
        }
      }

      @Override
      public void onDelete(Pod pod, boolean deletedFinalStateUnknown) {
        String podIp = pod.getStatus().getPodIP();
        if (podIp != null && pod.getMetadata().getLabels().containsValue(TARGET_LABEL)) {
          ipList.remove(podIp);
        }
      }
    };
  }

  @ConsumeEvent("newConfigEvent")
  public void onNewConfigEvent(UpdateInfo updateInfo) {
    httpClient.postUpdate(updateInfo, ipList);
  }
}
