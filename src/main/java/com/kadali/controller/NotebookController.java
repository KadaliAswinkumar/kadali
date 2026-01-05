package com.kadali.controller;

import com.kadali.dto.NotebookCreateRequest;
import com.kadali.dto.CellExecutionRequest;
import com.kadali.entity.Notebook;
import com.kadali.service.NotebookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notebooks")
@RequiredArgsConstructor
@Slf4j
public class NotebookController {
    
    private final NotebookService notebookService;
    
    @PostMapping
    public ResponseEntity<Notebook> createNotebook(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody NotebookCreateRequest request) {
        
        log.info("Creating notebook: {} for tenant: {}", request.getName(), tenantId);
        Notebook notebook = notebookService.createNotebook(
                tenantId, request.getName(), request.getLanguage());
        return ResponseEntity.status(HttpStatus.CREATED).body(notebook);
    }
    
    @GetMapping
    public ResponseEntity<List<Notebook>> listNotebooks(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        
        List<Notebook> notebooks = notebookService.listNotebooks(tenantId);
        return ResponseEntity.ok(notebooks);
    }
    
    @GetMapping("/{notebookId}")
    public ResponseEntity<Notebook> getNotebook(
            @PathVariable String notebookId) {
        
        Notebook notebook = notebookService.getNotebook(notebookId);
        return ResponseEntity.ok(notebook);
    }
    
    @PutMapping("/{notebookId}/content")
    public ResponseEntity<Notebook> updateContent(
            @PathVariable String notebookId,
            @RequestBody String content) {
        
        Notebook notebook = notebookService.updateNotebookContent(notebookId, content);
        return ResponseEntity.ok(notebook);
    }
    
    @PostMapping("/{notebookId}/attach-cluster")
    public ResponseEntity<Notebook> attachCluster(
            @PathVariable String notebookId,
            @RequestParam String clusterId) {
        
        Notebook notebook = notebookService.attachCluster(notebookId, clusterId);
        return ResponseEntity.ok(notebook);
    }
    
    @PostMapping("/{notebookId}/execute/python")
    public ResponseEntity<NotebookService.CellExecutionResult> executePythonCell(
            @PathVariable String notebookId,
            @Valid @RequestBody CellExecutionRequest request) {
        
        NotebookService.CellExecutionResult result = 
                notebookService.executePythonCell(notebookId, request.getCode());
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{notebookId}/execute/sql")
    public ResponseEntity<NotebookService.CellExecutionResult> executeSqlCell(
            @PathVariable String notebookId,
            @Valid @RequestBody CellExecutionRequest request) {
        
        NotebookService.CellExecutionResult result = 
                notebookService.executeSqlCell(notebookId, request.getCode());
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{notebookId}")
    public ResponseEntity<Void> deleteNotebook(
            @PathVariable String notebookId) {
        
        notebookService.deleteNotebook(notebookId);
        return ResponseEntity.noContent().build();
    }
}

