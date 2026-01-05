package com.kadali.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kadali.entity.Dataset;
import com.kadali.entity.Tenant;
import com.kadali.repository.DatasetRepository;
import com.kadali.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Catalog Service - Manages metadata for lakehouse tables
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataCatalogService {
    
    private final DatasetRepository datasetRepository;
    private final TenantRepository tenantRepository;
    private final SparkSession sparkSession;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public Dataset registerDataset(String tenantId, String databaseName, String tableName,
                                   String location, String format, String description) {
        log.info("Registering dataset: {}.{} for tenant: {}", databaseName, tableName, tenantId);
        
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        // Get schema from Spark
        org.apache.spark.sql.Dataset<org.apache.spark.sql.Row> df = 
                sparkSession.read().format(format).load(location);
        
        StructType schema = df.schema();
        String schemaJson = schema.json();
        
        long rowCount = df.count();
        
        Dataset dataset = Dataset.builder()
                .datasetId("ds-" + UUID.randomUUID().toString().substring(0, 8))
                .tenant(tenant)
                .databaseName(databaseName)
                .tableName(tableName)
                .location(location)
                .format(format)
                .schemaJson(schemaJson)
                .rowCount(rowCount)
                .description(description)
                .build();
        
        dataset = datasetRepository.save(dataset);
        
        log.info("Dataset registered: {}.{}", databaseName, tableName);
        return dataset;
    }
    
    public List<Dataset> listDatasets(String tenantId) {
        return datasetRepository.findByTenant_TenantId(tenantId);
    }
    
    public List<Dataset> listDatasetsByDatabase(String tenantId, String databaseName) {
        return datasetRepository.findByTenant_TenantIdAndDatabaseName(tenantId, databaseName);
    }
    
    public Dataset getDataset(String tenantId, String databaseName, String tableName) {
        return datasetRepository.findByTenant_TenantIdAndDatabaseNameAndTableName(
                tenantId, databaseName, tableName)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Dataset not found: %s.%s", databaseName, tableName)));
    }
    
    @Transactional
    public void updateDatasetStats(String datasetId) {
        Dataset dataset = datasetRepository.findByDatasetId(datasetId)
                .orElseThrow(() -> new RuntimeException("Dataset not found: " + datasetId));
        
        log.info("Updating stats for dataset: {}.{}", 
                dataset.getDatabaseName(), dataset.getTableName());
        
        try {
            org.apache.spark.sql.Dataset<org.apache.spark.sql.Row> df = 
                    sparkSession.read().format(dataset.getFormat()).load(dataset.getLocation());
            
            dataset.setRowCount(df.count());
            dataset.setLastAccessedAt(LocalDateTime.now());
            
            datasetRepository.save(dataset);
            
            log.info("Dataset stats updated: {}.{}", 
                    dataset.getDatabaseName(), dataset.getTableName());
            
        } catch (Exception e) {
            log.error("Failed to update dataset stats: {}", datasetId, e);
        }
    }
    
    @Transactional
    public void deleteDataset(String tenantId, String databaseName, String tableName) {
        Dataset dataset = getDataset(tenantId, databaseName, tableName);
        
        log.info("Deleting dataset: {}.{}", databaseName, tableName);
        
        // Drop table from Spark
        sparkSession.sql(String.format("DROP TABLE IF EXISTS %s.%s", databaseName, tableName));
        
        datasetRepository.delete(dataset);
        
        log.info("Dataset deleted: {}.{}", databaseName, tableName);
    }
    
    public void createDatabase(String tenantId, String databaseName) {
        log.info("Creating database: {} for tenant: {}", databaseName, tenantId);
        
        sparkSession.sql(String.format("CREATE DATABASE IF NOT EXISTS %s", databaseName));
        
        log.info("Database created: {}", databaseName);
    }
    
    public List<String> listDatabases(String tenantId) {
        org.apache.spark.sql.Dataset<org.apache.spark.sql.Row> databases = 
                sparkSession.sql("SHOW DATABASES");
        
        return databases.collectAsList().stream()
                .map(row -> row.getString(0))
                .filter(db -> db.startsWith("tenant_" + tenantId) || db.equals("default"))
                .toList();
    }
}

