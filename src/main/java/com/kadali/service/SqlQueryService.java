package com.kadali.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL Query Execution Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqlQueryService {
    
    private final SparkSession sparkSession;
    
    // In-memory query cache (in production, use Redis or similar)
    private final Map<String, QueryResult> queryCache = new ConcurrentHashMap<>();
    
    public QueryResult executeQuery(String queryId, String tenantId, String sql, int limit) {
        log.info("Executing query {} for tenant: {}", queryId, tenantId);
        
        QueryResult result = QueryResult.builder()
                .queryId(queryId)
                .status("RUNNING")
                .sql(sql)
                .startTime(LocalDateTime.now())
                .build();
        
        queryCache.put(queryId, result);
        
        try {
            // Execute query
            Dataset<Row> df = sparkSession.sql(sql);
            
            if (limit > 0) {
                df = df.limit(limit);
            }
            
            // Collect results
            List<Row> rows = df.collectAsList();
            String[] columns = df.columns();
            
            List<Map<String, Object>> data = new ArrayList<>();
            for (Row row : rows) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < columns.length; i++) {
                    rowMap.put(columns[i], row.get(i));
                }
                data.add(rowMap);
            }
            
            result.setColumns(Arrays.asList(columns));
            result.setData(data);
            result.setRowCount(data.size());
            result.setStatus("COMPLETED");
            result.setEndTime(LocalDateTime.now());
            
            log.info("Query {} completed successfully. Returned {} rows", queryId, data.size());
            
        } catch (Exception e) {
            log.error("Query {} failed", queryId, e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
        }
        
        queryCache.put(queryId, result);
        return result;
    }
    
    public QueryResult getQueryResult(String queryId) {
        QueryResult result = queryCache.get(queryId);
        if (result == null) {
            throw new RuntimeException("Query not found: " + queryId);
        }
        return result;
    }
    
    public void cancelQuery(String queryId) {
        log.info("Cancelling query: {}", queryId);
        QueryResult result = queryCache.get(queryId);
        if (result != null) {
            result.setStatus("CANCELLED");
            result.setEndTime(LocalDateTime.now());
        }
    }
    
    public Dataset<Row> executeQueryAndReturnDataFrame(String sql) {
        log.info("Executing query: {}", sql);
        return sparkSession.sql(sql);
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QueryResult {
        private String queryId;
        private String sql;
        private String status;
        private List<String> columns;
        private List<Map<String, Object>> data;
        private Integer rowCount;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}

