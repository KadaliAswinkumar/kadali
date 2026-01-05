package com.kadali.controller;

import com.kadali.dto.DatabaseSyncRequest;
import com.kadali.service.DataCatalogService;
import com.kadali.service.DataConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/connectors")
@RequiredArgsConstructor
@Slf4j
public class DataConnectorController {
    
    private final DataConnectorService connectorService;
    private final DataCatalogService catalogService;
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String database,
            @RequestParam String tableName,
            @RequestParam(defaultValue = "csv") String format) {
        
        try {
            String uploadId = connectorService.uploadFileAndSaveToDelta(
                    file, tenantId, database, tableName, format);
            
            Map<String, String> response = new HashMap<>();
            response.put("uploadId", uploadId);
            response.put("message", "File uploaded successfully");
            response.put("table", database + "." + tableName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("File upload failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/sync-database")
    public ResponseEntity<Map<String, String>> syncDatabase(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody DatabaseSyncRequest request) {
        
        try {
            connectorService.syncDatabaseTableToDelta(
                    request.getSourceType(),
                    request.getJdbcUrl(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getSourceTable(),
                    tenantId,
                    request.getTargetDatabase(),
                    request.getTargetTable()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Database sync completed");
            response.put("table", request.getTargetDatabase() + "." + request.getTargetTable());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Database sync failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/preview-csv")
    public ResponseEntity<Map<String, Object>> previewCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") boolean header) {
        
        try {
            // Save temporarily and preview
            String tempPath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
            file.transferTo(new java.io.File(tempPath));
            
            Dataset<Row> data = connectorService.loadCsvFile(tempPath, header, ",");
            
            Map<String, Object> response = new HashMap<>();
            response.put("schema", data.schema().treeString());
            response.put("rowCount", data.count());
            response.put("columns", data.columns());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("CSV preview failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

