package com.kadali.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "kadali.kubernetes.enabled", havingValue = "true")
@Slf4j
public class KubernetesConfig {
    
    @Value("${kadali.kubernetes.config-path:}")
    private String configPath;
    
    @Bean
    public KubernetesClient kubernetesClient() {
        log.info("Initializing Kubernetes client");
        
        Config config;
        if (configPath != null && !configPath.isEmpty()) {
            config = new ConfigBuilder()
                    .withKubeconfigPath(configPath)
                    .build();
        } else {
            // Use in-cluster configuration
            config = Config.autoConfigure(null);
        }
        
        KubernetesClient client = new KubernetesClientBuilder()
                .withConfig(config)
                .build();
        
        log.info("Kubernetes client initialized. Master URL: {}", client.getMasterUrl());
        return client;
    }
}

