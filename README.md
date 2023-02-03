# kkv v2 experiment

This project was an experiment and the findings have been backported to kkv.
See https://github.com/Yolean/kafka-keyvalue/pull/46.

`kkv` is a kubernetes-native bridge between a Kafka topic and your services.
For low throughput topics there is no need to embed kafka clients.
Application code gets notified about updates and can query for the latest value by key,
over HTTP or gRPC (TODO).

## Building

### With ystack

```
IMAGE=builds-registry.ystack.svc.cluster.local/yolean/kkv2 IMAGE_PUSH=true IMAGE_PUSH_INSECURE=true mvn package
```
