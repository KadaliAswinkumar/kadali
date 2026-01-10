package com.kadali.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
@ConditionalOnProperty(name = "kadali.kubernetes.enabled", havingValue = "true")
@Slf4j
public class KubernetesConfig {
    
    @Value("${kadali.kubernetes.config-path:}")
    private String configPath;
    
    @Bean
    public KubernetesClient kubernetesClient() {
        log.info("Initializing Kubernetes client");
        
        try {
            Config config;
            if (configPath != null && !configPath.isEmpty()) {
                // Load from specific kubeconfig file
                config = Config.fromKubeconfig(new FileInputStream(configPath));
            } else {
                // Use auto-configuration (from default kubeconfig or in-cluster)
                config = Config.autoConfigure(null);
            }
            
            KubernetesClient client = new KubernetesClientBuilder()
                    .withConfig(config)
                    .build();
            
            log.info("Kubernetes client initialized. Master URL: {}", client.getMasterUrl());
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize Kubernetes client", e);
            throw new RuntimeException("Failed to initialize Kubernetes client", e);
        }
    }
}

