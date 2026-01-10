package com.kadali.spark;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Spark cluster lifecycle on Kubernetes
 */
@Service
@ConditionalOnProperty(name = "kadali.kubernetes.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class K8sSparkClusterManager {
    
    private final KubernetesClient kubernetesClient;
    
    @Value("${kadali.spark.namespace}")
    private String sparkNamespace;
    
    @Value("${kadali.spark.docker-image}")
    private String sparkImage;
    
    @Value("${kadali.spark.service-account}")
    private String serviceAccount;
    
    public void createNamespaceIfNotExists(String namespace) {
        try {
            Namespace ns = kubernetesClient.namespaces()
                    .withName(namespace)
                    .get();
            
            if (ns == null) {
                log.info("Creating namespace: {}", namespace);
                kubernetesClient.namespaces().resource(
                        new NamespaceBuilder()
                                .withNewMetadata()
                                .withName(namespace)
                                .endMetadata()
                                .build()
                ).create();
            }
        } catch (Exception e) {
            log.error("Error creating namespace: {}", namespace, e);
            throw new RuntimeException("Failed to create namespace", e);
        }
    }
    
    public Pod createSparkDriverPod(String clusterId, String tenantId, 
                                     String driverMemory, int driverCores,
                                     String executorMemory, int executorCores, int executorCount) {
        try {
            String namespace = "kadali-" + tenantId;
            createNamespaceIfNotExists(namespace);
            
            String podName = "spark-driver-" + clusterId;
            
            Map<String, String> labels = new HashMap<>();
            labels.put("app", "spark");
            labels.put("component", "driver");
            labels.put("cluster-id", clusterId);
            labels.put("tenant-id", tenantId);
            
            Map<String, String> sparkConf = new HashMap<>();
            sparkConf.put("spark.master", "k8s://https://kubernetes.default.svc:443");
            sparkConf.put("spark.kubernetes.container.image", sparkImage);
            sparkConf.put("spark.kubernetes.namespace", namespace);
            sparkConf.put("spark.executor.instances", String.valueOf(executorCount));
            sparkConf.put("spark.executor.memory", executorMemory);
            sparkConf.put("spark.executor.cores", String.valueOf(executorCores));
            sparkConf.put("spark.driver.memory", driverMemory);
            sparkConf.put("spark.driver.cores", String.valueOf(driverCores));
            
            Pod driverPod = new PodBuilder()
                    .withNewMetadata()
                        .withName(podName)
                        .withNamespace(namespace)
                        .withLabels(labels)
                    .endMetadata()
                    .withNewSpec()
                        .withServiceAccount(serviceAccount)
                        .withRestartPolicy("Never")
                        .addNewContainer()
                            .withName("spark-driver")
                            .withImage(sparkImage)
                            .withCommand("/opt/spark/bin/spark-submit")
                            .withArgs(
                                "--master", "k8s://https://kubernetes.default.svc:443",
                                "--deploy-mode", "client",
                                "--name", clusterId,
                                "--conf", "spark.kubernetes.namespace=" + namespace,
                                "--conf", "spark.executor.instances=" + executorCount,
                                "--conf", "spark.executor.memory=" + executorMemory,
                                "--conf", "spark.executor.cores=" + executorCores,
                                "--conf", "spark.kubernetes.container.image=" + sparkImage,
                                "--class", "org.apache.spark.repl.Main",
                                "spark-shell"
                            )
                            .withNewResources()
                                .withRequests(Map.of(
                                    "memory", new Quantity(driverMemory),
                                    "cpu", new Quantity(String.valueOf(driverCores))
                                ))
                                .withLimits(Map.of(
                                    "memory", new Quantity(driverMemory),
                                    "cpu", new Quantity(String.valueOf(driverCores))
                                ))
                            .endResources()
                            .addNewPort()
                                .withContainerPort(4040)
                                .withName("spark-ui")
                            .endPort()
                        .endContainer()
                    .endSpec()
                    .build();
            
            log.info("Creating Spark driver pod: {} in namespace: {}", podName, namespace);
            Pod createdPod = kubernetesClient.pods()
                    .inNamespace(namespace)
                    .resource(driverPod)
                    .create();
            
            log.info("Spark driver pod created successfully: {}", createdPod.getMetadata().getName());
            return createdPod;
            
        } catch (Exception e) {
            log.error("Error creating Spark driver pod for cluster: {}", clusterId, e);
            throw new RuntimeException("Failed to create Spark cluster", e);
        }
    }
    
    public void deleteSparkCluster(String clusterId, String tenantId) {
        try {
            String namespace = "kadali-" + tenantId;
            
            log.info("Deleting Spark cluster: {} in namespace: {}", clusterId, namespace);
            
            // Delete driver pod
            kubernetesClient.pods()
                    .inNamespace(namespace)
                    .withLabel("cluster-id", clusterId)
                    .delete();
            
            log.info("Spark cluster deleted: {}", clusterId);
            
        } catch (Exception e) {
            log.error("Error deleting Spark cluster: {}", clusterId, e);
            throw new RuntimeException("Failed to delete Spark cluster", e);
        }
    }
    
    public Pod getDriverPod(String clusterId, String tenantId) {
        String namespace = "kadali-" + tenantId;
        return kubernetesClient.pods()
                .inNamespace(namespace)
                .withLabel("cluster-id", clusterId)
                .withLabel("component", "driver")
                .list()
                .getItems()
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    public String getSparkUIUrl(String clusterId, String tenantId) {
        Pod pod = getDriverPod(clusterId, tenantId);
        if (pod != null && pod.getStatus().getPodIP() != null) {
            return "http://" + pod.getStatus().getPodIP() + ":4040";
        }
        return null;
    }
}

