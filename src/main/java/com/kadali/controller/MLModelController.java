package com.kadali.controller;

import com.kadali.dto.ModelRegisterRequest;
import com.kadali.dto.ModelPredictRequest;
import com.kadali.service.MLModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
@Slf4j
public class MLModelController {
    
    private final MLModelService mlModelService;
    
    @PostMapping
    public ResponseEntity<MLModelService.MLModel> registerModel(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody ModelRegisterRequest request) {
        
        MLModelService.MLModel model = mlModelService.registerModel(
                tenantId,
                request.getName(),
                request.getVersion(),
                request.getFramework(),
                request.getAlgorithm(),
                request.getMetrics(),
                request.getParams(),
                request.getArtifactPath()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
    
    @GetMapping
    public ResponseEntity<List<MLModelService.MLModel>> listModels(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        
        List<MLModelService.MLModel> models = mlModelService.listModels(tenantId);
        return ResponseEntity.ok(models);
    }
    
    @GetMapping("/{modelId}")
    public ResponseEntity<MLModelService.MLModel> getModel(
            @PathVariable String modelId) {
        
        MLModelService.MLModel model = mlModelService.getModel(modelId);
        return ResponseEntity.ok(model);
    }
    
    @PutMapping("/{modelId}/stage")
    public ResponseEntity<MLModelService.MLModel> updateStage(
            @PathVariable String modelId,
            @RequestParam String stage) {
        
        MLModelService.MLModel model = mlModelService.updateModelStage(modelId, stage);
        return ResponseEntity.ok(model);
    }
    
    @PostMapping("/{modelId}/predict")
    public ResponseEntity<Map<String, Object>> predict(
            @PathVariable String modelId,
            @Valid @RequestBody ModelPredictRequest request) {
        
        Map<String, Object> predictions = mlModelService.predictBatch(
                modelId, request.getInputData());
        return ResponseEntity.ok(predictions);
    }
    
    @DeleteMapping("/{modelId}")
    public ResponseEntity<Void> deleteModel(
            @PathVariable String modelId) {
        
        mlModelService.deleteModel(modelId);
        return ResponseEntity.noContent().build();
    }
}

