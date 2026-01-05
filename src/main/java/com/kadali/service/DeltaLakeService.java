package com.kadali.service;

import io.delta.tables.DeltaTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for Delta Lake operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeltaLakeService {
    
    private final SparkSession sparkSession;
    
    @Value("${kadali.storage.lakehouse-path}")
    private String lakehousePath;
    
    public void createDeltaTable(String tenantId, String database, String tableName, 
                                 Dataset<Row> data, String[] partitionColumns) {
        String tablePath = getTablePath(tenantId, database, tableName);
        
        log.info("Creating Delta table: {}.{} at path: {}", database, tableName, tablePath);
        
        try {
            if (partitionColumns != null && partitionColumns.length > 0) {
                data.write()
                        .format("delta")
                        .mode(SaveMode.ErrorIfExists)
                        .partitionBy(partitionColumns)
                        .save(tablePath);
            } else {
                data.write()
                        .format("delta")
                        .mode(SaveMode.ErrorIfExists)
                        .save(tablePath);
            }
            
            // Register table in metastore
            sparkSession.sql(String.format(
                    "CREATE TABLE IF NOT EXISTS %s.%s USING DELTA LOCATION '%s'",
                    database, tableName, tablePath
            ));
            
            log.info("Delta table created successfully: {}.{}", database, tableName);
            
        } catch (Exception e) {
            log.error("Failed to create Delta table: {}.{}", database, tableName, e);
            throw new RuntimeException("Failed to create Delta table", e);
        }
    }
    
    public Dataset<Row> readDeltaTable(String tenantId, String database, String tableName) {
        String tablePath = getTablePath(tenantId, database, tableName);
        
        log.info("Reading Delta table: {}.{}", database, tableName);
        
        try {
            return sparkSession.read()
                    .format("delta")
                    .load(tablePath);
        } catch (Exception e) {
            log.error("Failed to read Delta table: {}.{}", database, tableName, e);
            throw new RuntimeException("Failed to read Delta table", e);
        }
    }
    
    public void appendToDeltaTable(String tenantId, String database, String tableName, 
                                   Dataset<Row> data) {
        String tablePath = getTablePath(tenantId, database, tableName);
        
        log.info("Appending data to Delta table: {}.{}", database, tableName);
        
        try {
            data.write()
                    .format("delta")
                    .mode(SaveMode.Append)
                    .save(tablePath);
            
            log.info("Data appended successfully to: {}.{}", database, tableName);
            
        } catch (Exception e) {
            log.error("Failed to append to Delta table: {}.{}", database, tableName, e);
            throw new RuntimeException("Failed to append to Delta table", e);
        }
    }
    
    public void updateDeltaTable(String tenantId, String database, String tableName,
                                 String condition, Map<String, String> updates) {
        String tablePath = getTablePath(tenantId, database, tableName);
        
        log.info("Updating Delta table: {}.{} with condition: {}", database, tableName, condition);
        
        try {
            DeltaTable deltaTable = DeltaTable.forPath(sparkSession, tablePath);
            
            // Build update map
            Map<String, org.apache.spark.sql.Column> updateMap = new HashMap<>();
            updates.forEach((key, value) -> 
                updateMap.put(key, org.apache.spark.sql.functions.lit(value))
            );
            
            deltaTable.update(
                    org.apache.spark.sql.functions.expr(condition),
                    updateMap
            );
            
            log.info("Delta table updated successfully: {}.{}", database, tableName);
            
        } catch (Exception e) {
            log.error("Failed to update Delta table: {}.{}", database, tableName, e);
            throw new RuntimeException("Failed to update Delta table", e);
        }
    }
    
    public void deletefromDeltaTable(String tenantId, String database, String tableName,
                                     String condition) {
        String tablePath = getTablePath(tenantId, database, tableName);
        
        log.info("Deleting from Delta table: {}.{} with condition: {}", database, tableName, condition);
        
        try {
            DeltaTable deltaTable = DeltaTable.forPath(sparkSession, tablePath);
            deltaTable.delete(org.apache.spark.sql.functions.expr(condition));
            
            log.info("Deleted from Delta table: {}.{}", database, tableName);
            
        } catch (Exception e) {
            log.error("Failed to delete from Delta table: {}.{}", database, tableName, e);
            throw new RuntimeException("Failed to delete from Delta table", e);
        }
    }
    
    public Dataset<Row> timeTravel(String tenantId, String database, String tableName, 
                                   long version) {
        String tablePath = getTablePath(tenantId, database, tableName);
        
        log.info("Time traveling to version {} for table: {}.{}", version, database, tableName);
        
        try {
            return sparkSession.read()
                    .format("delta")
                    .option("versionAsOf", version)
                    .load(tablePath);
        } catch (Exception e) {
            log.error("Failed to time travel: {}.{}", database, tableName, e);
            throw new RuntimeException("Failed to time travel", e);
        }
    }
    
    public void vacuumDeltaTable(String tenantId, String database, String tableName, 
                                 int retentionHours) {
        String tablePath = getTablePath(tenantId, database, tableName);
        
        log.info("Vacuuming Delta table: {}.{} with retention: {} hours", 
                database, tableName, retentionHours);
        
        try {
            DeltaTable deltaTable = DeltaTable.forPath(sparkSession, tablePath);
            deltaTable.vacuum(retentionHours);
            
            log.info("Vacuumed Delta table: {}.{}", database, tableName);
            
        } catch (Exception e) {
            log.error("Failed to vacuum Delta table: {}.{}", database, tableName, e);
            throw new RuntimeException("Failed to vacuum Delta table", e);
        }
    }
    
    private String getTablePath(String tenantId, String database, String tableName) {
        return String.format("%stenant-%s/%s/%s", lakehousePath, tenantId, database, tableName);
    }
}

