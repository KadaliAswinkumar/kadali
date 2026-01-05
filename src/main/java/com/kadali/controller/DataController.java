package com.kadali.controller;

import com.kadali.dto.QueryRequest;
import com.kadali.entity.Dataset;
import com.kadali.service.DataCatalogService;
import com.kadali.service.SqlQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Slf4j
public class DataController {
    
    private final DataCatalogService catalogService;
    private final SqlQueryService queryService;
    
    // Database operations
    @PostMapping("/databases")
    public ResponseEntity<Void> createDatabase(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String databaseName) {
        
        catalogService.createDatabase(tenantId, databaseName);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/databases")
    public ResponseEntity<List<String>> listDatabases(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        
        List<String> databases = catalogService.listDatabases(tenantId);
        return ResponseEntity.ok(databases);
    }
    
    // Dataset operations
    @GetMapping("/datasets")
    public ResponseEntity<List<Dataset>> listDatasets(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam(required = false) String database) {
        
        List<Dataset> datasets;
        if (database != null) {
            datasets = catalogService.listDatasetsByDatabase(tenantId, database);
        } else {
            datasets = catalogService.listDatasets(tenantId);
        }
        return ResponseEntity.ok(datasets);
    }
    
    @GetMapping("/datasets/{database}/{table}")
    public ResponseEntity<Dataset> getDataset(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String database,
            @PathVariable String table) {
        
        Dataset dataset = catalogService.getDataset(tenantId, database, table);
        return ResponseEntity.ok(dataset);
    }
    
    @DeleteMapping("/datasets/{database}/{table}")
    public ResponseEntity<Void> deleteDataset(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String database,
            @PathVariable String table) {
        
        catalogService.deleteDataset(tenantId, database, table);
        return ResponseEntity.noContent().build();
    }
    
    // Query execution
    @PostMapping("/query")
    public ResponseEntity<SqlQueryService.QueryResult> executeQuery(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody QueryRequest request) {
        
        String queryId = "query-" + UUID.randomUUID().toString().substring(0, 8);
        SqlQueryService.QueryResult result = queryService.executeQuery(
                queryId, tenantId, request.getSql(), request.getLimit());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/query/{queryId}")
    public ResponseEntity<SqlQueryService.QueryResult> getQueryResult(
            @PathVariable String queryId) {
        
        SqlQueryService.QueryResult result = queryService.getQueryResult(queryId);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/query/{queryId}")
    public ResponseEntity<Void> cancelQuery(
            @PathVariable String queryId) {
        
        queryService.cancelQuery(queryId);
        return ResponseEntity.noContent().build();
    }
}

