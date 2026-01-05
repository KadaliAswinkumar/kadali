package com.kadali.repository;

import com.kadali.entity.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    Optional<Dataset> findByDatasetId(String datasetId);
    List<Dataset> findByTenant_TenantId(String tenantId);
    List<Dataset> findByTenant_TenantIdAndDatabaseName(String tenantId, String databaseName);
    Optional<Dataset> findByTenant_TenantIdAndDatabaseNameAndTableName(
        String tenantId, String databaseName, String tableName);
}

