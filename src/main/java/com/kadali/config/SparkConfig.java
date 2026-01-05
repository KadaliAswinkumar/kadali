package com.kadali.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SparkConfig {
    
    @Value("${kadali.storage.lakehouse-path}")
    private String lakehousePath;
    
    @Value("${kadali.storage.endpoint}")
    private String storageEndpoint;
    
    @Value("${kadali.storage.access-key}")
    private String accessKey;
    
    @Value("${kadali.storage.secret-key}")
    private String secretKey;
    
    @Value("${kadali.metastore.uri}")
    private String metastoreUri;
    
    @Bean
    public SparkSession sparkSession() {
        log.info("Initializing Spark Session with Delta Lake support");
        
        SparkConf conf = new SparkConf()
                .setAppName("Kadali Data Platform")
                .setMaster("local[*]") // For local development
                // Delta Lake
                .set("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
                .set("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
                // S3/MinIO configuration
                .set("spark.hadoop.fs.s3a.endpoint", storageEndpoint)
                .set("spark.hadoop.fs.s3a.access.key", accessKey)
                .set("spark.hadoop.fs.s3a.secret.key", secretKey)
                .set("spark.hadoop.fs.s3a.path.style.access", "true")
                .set("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
                .set("spark.hadoop.fs.s3a.connection.ssl.enabled", "false")
                // Hive Metastore
                .set("spark.sql.catalogImplementation", "hive")
                .set("spark.sql.warehouse.dir", lakehousePath)
                .set("hive.metastore.uris", metastoreUri);
        
        SparkSession spark = SparkSession.builder()
                .config(conf)
                .enableHiveSupport()
                .getOrCreate();
        
        log.info("Spark Session initialized successfully");
        log.info("Lakehouse path: {}", lakehousePath);
        log.info("Metastore URI: {}", metastoreUri);
        
        return spark;
    }
}

