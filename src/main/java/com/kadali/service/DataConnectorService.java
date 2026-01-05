package com.kadali.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Data connector service for various data sources
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataConnectorService {
    
    private final SparkSession sparkSession;
    private final DeltaLakeService deltaLakeService;
    
    /**
     * Load data from PostgreSQL
     */
    public Dataset<Row> loadFromPostgres(String jdbcUrl, String username, String password,
                                         String tableName) {
        log.info("Loading data from PostgreSQL table: {}", tableName);
        
        try {
            return sparkSession.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", tableName)
                    .option("user", username)
                    .option("password", password)
                    .option("driver", "org.postgresql.Driver")
                    .load();
        } catch (Exception e) {
            log.error("Failed to load from PostgreSQL: {}", tableName, e);
            throw new RuntimeException("Failed to load from PostgreSQL", e);
        }
    }
    
    /**
     * Load data from MySQL
     */
    public Dataset<Row> loadFromMySQL(String jdbcUrl, String username, String password,
                                      String tableName) {
        log.info("Loading data from MySQL table: {}", tableName);
        
        try {
            return sparkSession.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", tableName)
                    .option("user", username)
                    .option("password", password)
                    .option("driver", "com.mysql.cj.jdbc.Driver")
                    .load();
        } catch (Exception e) {
            log.error("Failed to load from MySQL: {}", tableName, e);
            throw new RuntimeException("Failed to load from MySQL", e);
        }
    }
    
    /**
     * Load data from MongoDB
     */
    public Dataset<Row> loadFromMongoDB(String mongoUri, String database, String collection) {
        log.info("Loading data from MongoDB collection: {}.{}", database, collection);
        
        try {
            return sparkSession.read()
                    .format("mongodb")
                    .option("spark.mongodb.input.uri", mongoUri)
                    .option("spark.mongodb.input.database", database)
                    .option("spark.mongodb.input.collection", collection)
                    .load();
        } catch (Exception e) {
            log.error("Failed to load from MongoDB: {}.{}", database, collection, e);
            throw new RuntimeException("Failed to load from MongoDB", e);
        }
    }
    
    /**
     * Load CSV file
     */
    public Dataset<Row> loadCsvFile(String path, boolean header, String delimiter) {
        log.info("Loading CSV file: {}", path);
        
        try {
            return sparkSession.read()
                    .format("csv")
                    .option("header", header)
                    .option("delimiter", delimiter)
                    .option("inferSchema", true)
                    .load(path);
        } catch (Exception e) {
            log.error("Failed to load CSV file: {}", path, e);
            throw new RuntimeException("Failed to load CSV file", e);
        }
    }
    
    /**
     * Load Parquet file
     */
    public Dataset<Row> loadParquetFile(String path) {
        log.info("Loading Parquet file: {}", path);
        
        try {
            return sparkSession.read()
                    .format("parquet")
                    .load(path);
        } catch (Exception e) {
            log.error("Failed to load Parquet file: {}", path, e);
            throw new RuntimeException("Failed to load Parquet file", e);
        }
    }
    
    /**
     * Load JSON file
     */
    public Dataset<Row> loadJsonFile(String path) {
        log.info("Loading JSON file: {}", path);
        
        try {
            return sparkSession.read()
                    .format("json")
                    .load(path);
        } catch (Exception e) {
            log.error("Failed to load JSON file: {}", path, e);
            throw new RuntimeException("Failed to load JSON file", e);
        }
    }
    
    /**
     * Load data from S3
     */
    public Dataset<Row> loadFromS3(String s3Path, String format) {
        log.info("Loading data from S3: {}", s3Path);
        
        try {
            return sparkSession.read()
                    .format(format)
                    .load(s3Path);
        } catch (Exception e) {
            log.error("Failed to load from S3: {}", s3Path, e);
            throw new RuntimeException("Failed to load from S3", e);
        }
    }
    
    /**
     * Save data to PostgreSQL
     */
    public void saveToPostgres(Dataset<Row> data, String jdbcUrl, String username, 
                               String password, String tableName, SaveMode saveMode) {
        log.info("Saving data to PostgreSQL table: {}", tableName);
        
        try {
            data.write()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", tableName)
                    .option("user", username)
                    .option("password", password)
                    .option("driver", "org.postgresql.Driver")
                    .mode(saveMode)
                    .save();
            
            log.info("Data saved to PostgreSQL: {}", tableName);
        } catch (Exception e) {
            log.error("Failed to save to PostgreSQL: {}", tableName, e);
            throw new RuntimeException("Failed to save to PostgreSQL", e);
        }
    }
    
    /**
     * Upload file and save to Delta Lake
     */
    public String uploadFileAndSaveToDelta(MultipartFile file, String tenantId, 
                                          String database, String tableName, 
                                          String format) throws IOException {
        log.info("Uploading file: {} for tenant: {}", file.getOriginalFilename(), tenantId);
        
        // Save file temporarily
        String tempDir = System.getProperty("java.io.tmpdir");
        String uploadId = UUID.randomUUID().toString();
        String fileName = uploadId + "_" + file.getOriginalFilename();
        Path tempPath = Path.of(tempDir, fileName);
        
        file.transferTo(tempPath.toFile());
        
        try {
            // Load file based on format
            Dataset<Row> data;
            switch (format.toLowerCase()) {
                case "csv":
                    data = loadCsvFile(tempPath.toString(), true, ",");
                    break;
                case "parquet":
                    data = loadParquetFile(tempPath.toString());
                    break;
                case "json":
                    data = loadJsonFile(tempPath.toString());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
            
            // Save to Delta Lake
            deltaLakeService.createDeltaTable(tenantId, database, tableName, data, null);
            
            log.info("File uploaded and saved to Delta Lake: {}.{}", database, tableName);
            return uploadId;
            
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempPath);
        }
    }
    
    /**
     * Sync data from external database to Delta Lake
     */
    public void syncDatabaseTableToDelta(String sourceType, String jdbcUrl, 
                                        String username, String password, 
                                        String sourceTable, String tenantId,
                                        String targetDatabase, String targetTable) {
        log.info("Syncing {} table {} to Delta Lake", sourceType, sourceTable);
        
        Dataset<Row> data;
        switch (sourceType.toLowerCase()) {
            case "postgres":
                data = loadFromPostgres(jdbcUrl, username, password, sourceTable);
                break;
            case "mysql":
                data = loadFromMySQL(jdbcUrl, username, password, sourceTable);
                break;
            default:
                throw new IllegalArgumentException("Unsupported source type: " + sourceType);
        }
        
        deltaLakeService.createDeltaTable(tenantId, targetDatabase, targetTable, data, null);
        
        log.info("Data synced from {} to Delta Lake: {}.{}", 
                sourceType, targetDatabase, targetTable);
    }
    
    /**
     * Export Delta table to external database
     */
    public void exportDeltaToDatabase(String tenantId, String database, String tableName,
                                     String targetType, String jdbcUrl, 
                                     String username, String password, 
                                     String targetTable, SaveMode saveMode) {
        log.info("Exporting Delta table {}.{} to {}", database, tableName, targetType);
        
        Dataset<Row> data = deltaLakeService.readDeltaTable(tenantId, database, tableName);
        
        switch (targetType.toLowerCase()) {
            case "postgres":
                saveToPostgres(data, jdbcUrl, username, password, targetTable, saveMode);
                break;
            case "mysql":
                saveToPostgres(data, jdbcUrl, username, password, targetTable, saveMode);
                break;
            default:
                throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
        
        log.info("Data exported from Delta Lake to {}: {}", targetType, targetTable);
    }
}

