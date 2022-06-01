export VERSION=$(curl -s https://api.github.com/repos/redpanda-data/redpanda/releases/latest | jq -r .tag_name)

k3d cluster create --config ./cluster/SimpleConfig.yaml

k apply -f https://github.com/jetstack/cert-manager/releases/download/v1.4.0/cert-manager.yaml
helm repo add redpanda https://charts.vectorized.io/ && helm repo update
kubectl apply -k https://github.com/redpanda-data/redpanda/src/go/k8s/config/crd?ref=$VERSION
helm install redpanda-operator redpanda/redpanda-operator --namespace redpanda-system --create-namespace --version $VERSION
kubectl create namespace kkv

# This step fails very often. Usually I recommend to run this step manually.
kubectl apply -f https://raw.githubusercontent.com/redpanda-data/redpanda/dev/src/go/k8s/config/samples/one_node_cluster.yaml

kubectl exec -n kkv -it redpanda-0 bash
rpk topic create config
exit

skaffold run