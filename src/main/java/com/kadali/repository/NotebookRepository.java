package com.kadali.repository;

import com.kadali.entity.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Long> {
    Optional<Notebook> findByNotebookId(String notebookId);
    List<Notebook> findByTenant_TenantId(String tenantId);
    List<Notebook> findByCluster_ClusterId(String clusterId);
}

