package com.kadali.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kadali.entity.Notebook;
import com.kadali.entity.SparkCluster;
import com.kadali.entity.Tenant;
import com.kadali.entity.User;
import com.kadali.repository.NotebookRepository;
import com.kadali.repository.SparkClusterRepository;
import com.kadali.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Notebook execution service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotebookService {
    
    private final NotebookRepository notebookRepository;
    private final TenantRepository tenantRepository;
    private final SparkClusterRepository clusterRepository;
    private final SparkSession sparkSession;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public Notebook createNotebook(String tenantId, String name, String language) {
        log.info("Creating notebook: {} for tenant: {}", name, tenantId);
        
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        
        String notebookId = "nb-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Initial empty notebook content
        Map<String, Object> notebookContent = new HashMap<>();
        notebookContent.put("cells", new ArrayList<>());
        notebookContent.put("metadata", Map.of(
            "language", language,
            "kernelspec", Map.of(
                "name", language.equals("python") ? "pyspark" : language,
                "display_name", language.equals("python") ? "PySpark" : language.toUpperCase()
            )
        ));
        
        try {
            String contentJson = objectMapper.writeValueAsString(notebookContent);
            
            Notebook notebook = Notebook.builder()
                    .notebookId(notebookId)
                    .tenant(tenant)
                    .name(name)
                    .language(language)
                    .kernelType(language.equals("python") ? "pyspark" : language)
                    .content(contentJson)
                    .version(1)
                    .build();
            
            notebook = notebookRepository.save(notebook);
            log.info("Notebook created: {}", notebookId);
            return notebook;
            
        } catch (Exception e) {
            log.error("Failed to create notebook", e);
            throw new RuntimeException("Failed to create notebook", e);
        }
    }
    
    @Transactional
    public Notebook updateNotebookContent(String notebookId, String content) {
        Notebook notebook = notebookRepository.findByNotebookId(notebookId)
                .orElseThrow(() -> new RuntimeException("Notebook not found: " + notebookId));
        
        notebook.setContent(content);
        notebook.setVersion(notebook.getVersion() + 1);
        
        return notebookRepository.save(notebook);
    }
    
    @Transactional
    public Notebook attachCluster(String notebookId, String clusterId) {
        Notebook notebook = notebookRepository.findByNotebookId(notebookId)
                .orElseThrow(() -> new RuntimeException("Notebook not found: " + notebookId));
        
        SparkCluster cluster = clusterRepository.findByClusterId(clusterId)
                .orElseThrow(() -> new RuntimeException("Cluster not found: " + clusterId));
        
        notebook.setCluster(cluster);
        return notebookRepository.save(notebook);
    }
    
    public CellExecutionResult executePythonCell(String notebookId, String code) {
        Notebook notebook = notebookRepository.findByNotebookId(notebookId)
                .orElseThrow(() -> new RuntimeException("Notebook not found: " + notebookId));
        
        log.info("Executing Python cell in notebook: {}", notebookId);
        
        CellExecutionResult result = CellExecutionResult.builder()
                .status("RUNNING")
                .startTime(LocalDateTime.now())
                .build();
        
        try {
            // For Python code, we'll use Spark's Python API via SQL
            // In a real implementation, you'd use Py4J or Jupyter kernel
            
            // For now, handle SQL-style code
            if (code.trim().toLowerCase().startsWith("select") || 
                code.trim().toLowerCase().startsWith("show") ||
                code.trim().toLowerCase().startsWith("describe")) {
                
                Dataset<Row> df = sparkSession.sql(code);
                List<Row> rows = df.limit(100).collectAsList();
                String[] columns = df.columns();
                
                List<Map<String, Object>> data = new ArrayList<>();
                for (Row row : rows) {
                    Map<String, Object> rowMap = new HashMap<>();
                    for (int i = 0; i < columns.length; i++) {
                        rowMap.put(columns[i], row.get(i));
                    }
                    data.add(rowMap);
                }
                
                result.setOutputType("table");
                result.setOutput(data);
                result.setStatus("COMPLETED");
                
            } else {
                // For other Python code, return a message
                result.setOutputType("text");
                result.setOutput("Python code execution requires Jupyter kernel. " +
                        "This cell contains: " + code.substring(0, Math.min(100, code.length())));
                result.setStatus("COMPLETED");
            }
            
            result.setEndTime(LocalDateTime.now());
            
            // Update notebook execution stats
            notebook.setLastExecutedAt(LocalDateTime.now());
            notebook.setExecutionCount(notebook.getExecutionCount() + 1);
            notebookRepository.save(notebook);
            
            log.info("Cell execution completed for notebook: {}", notebookId);
            
        } catch (Exception e) {
            log.error("Cell execution failed for notebook: {}", notebookId, e);
            result.setStatus("ERROR");
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
        }
        
        return result;
    }
    
    public CellExecutionResult executeSqlCell(String notebookId, String sql) {
        Notebook notebook = notebookRepository.findByNotebookId(notebookId)
                .orElseThrow(() -> new RuntimeException("Notebook not found: " + notebookId));
        
        log.info("Executing SQL cell in notebook: {}", notebookId);
        
        CellExecutionResult result = CellExecutionResult.builder()
                .status("RUNNING")
                .startTime(LocalDateTime.now())
                .build();
        
        try {
            Dataset<Row> df = sparkSession.sql(sql);
            List<Row> rows = df.limit(100).collectAsList();
            String[] columns = df.columns();
            
            List<Map<String, Object>> data = new ArrayList<>();
            for (Row row : rows) {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < columns.length; i++) {
                    rowMap.put(columns[i], row.get(i));
                }
                data.add(rowMap);
            }
            
            result.setOutputType("table");
            result.setOutput(data);
            result.setStatus("COMPLETED");
            result.setRowCount(data.size());
            result.setEndTime(LocalDateTime.now());
            
            // Update notebook stats
            notebook.setLastExecutedAt(LocalDateTime.now());
            notebook.setExecutionCount(notebook.getExecutionCount() + 1);
            notebookRepository.save(notebook);
            
            log.info("SQL cell execution completed for notebook: {}", notebookId);
            
        } catch (Exception e) {
            log.error("SQL cell execution failed for notebook: {}", notebookId, e);
            result.setStatus("ERROR");
            result.setErrorMessage(e.getMessage());
            result.setEndTime(LocalDateTime.now());
        }
        
        return result;
    }
    
    public List<Notebook> listNotebooks(String tenantId) {
        return notebookRepository.findByTenant_TenantId(tenantId);
    }
    
    public Notebook getNotebook(String notebookId) {
        return notebookRepository.findByNotebookId(notebookId)
                .orElseThrow(() -> new RuntimeException("Notebook not found: " + notebookId));
    }
    
    @Transactional
    public void deleteNotebook(String notebookId) {
        Notebook notebook = getNotebook(notebookId);
        notebookRepository.delete(notebook);
        log.info("Notebook deleted: {}", notebookId);
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CellExecutionResult {
        private String status;
        private String outputType; // text, table, image, error
        private Object output;
        private Integer rowCount;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}

