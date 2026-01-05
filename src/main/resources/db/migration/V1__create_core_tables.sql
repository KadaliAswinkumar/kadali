-- Tenants (workspaces/organizations)
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    tier VARCHAR(20) NOT NULL, -- FREE, STARTUP, GROWTH, ENTERPRISE
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    storage_quota_gb INTEGER NOT NULL DEFAULT 10,
    compute_quota_cores INTEGER NOT NULL DEFAULT 2,
    lakehouse_path VARCHAR(500),
    metastore_database VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenants_tenant_id ON tenants(tenant_id);
CREATE INDEX idx_tenants_status ON tenants(status);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'DEVELOPER',
    jupyter_token VARCHAR(500),
    api_key_hash VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    UNIQUE(tenant_id, email)
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);

-- Spark Clusters
CREATE TABLE IF NOT EXISTS spark_clusters (
    id BIGSERIAL PRIMARY KEY,
    cluster_id VARCHAR(100) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    cluster_type VARCHAR(20) NOT NULL, -- INTERACTIVE, JOB, ML
    status VARCHAR(50) NOT NULL, -- CREATING, RUNNING, IDLE, TERMINATING, TERMINATED, ERROR
    
    -- Resources
    driver_memory VARCHAR(20),
    driver_cores INTEGER,
    executor_memory VARCHAR(20),
    executor_cores INTEGER,
    executor_count INTEGER,
    
    -- Kubernetes details
    namespace VARCHAR(100),
    driver_pod_name VARCHAR(200),
    spark_ui_url VARCHAR(500),
    
    -- Metadata
    created_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    terminated_at TIMESTAMP,
    last_activity_at TIMESTAMP,
    auto_terminate_minutes INTEGER DEFAULT 60
);

CREATE INDEX idx_clusters_tenant_id ON spark_clusters(tenant_id);
CREATE INDEX idx_clusters_status ON spark_clusters(status);
CREATE INDEX idx_clusters_cluster_id ON spark_clusters(cluster_id);

-- Notebooks
CREATE TABLE IF NOT EXISTS notebooks (
    id BIGSERIAL PRIMARY KEY,
    notebook_id VARCHAR(100) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    path VARCHAR(500),
    
    -- Notebook content
    kernel_type VARCHAR(50) NOT NULL DEFAULT 'pyspark',
    language VARCHAR(20) NOT NULL DEFAULT 'python',
    content TEXT, -- JSON format
    version INTEGER NOT NULL DEFAULT 1,
    
    -- Execution
    cluster_id BIGINT REFERENCES spark_clusters(id),
    last_executed_at TIMESTAMP,
    execution_count INTEGER DEFAULT 0,
    
    -- Collaboration
    created_by_user_id BIGINT REFERENCES users(id),
    shared_with_user_ids BIGINT[],
    is_public BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notebooks_tenant_id ON notebooks(tenant_id);
CREATE INDEX idx_notebooks_cluster_id ON notebooks(cluster_id);
CREATE INDEX idx_notebooks_created_by ON notebooks(created_by_user_id);

-- Datasets (Lakehouse tables)
CREATE TABLE IF NOT EXISTS datasets (
    id BIGSERIAL PRIMARY KEY,
    dataset_id VARCHAR(100) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    database_name VARCHAR(100) NOT NULL,
    table_name VARCHAR(200) NOT NULL,
    
    -- Storage
    location VARCHAR(1000) NOT NULL,
    format VARCHAR(50) NOT NULL DEFAULT 'delta',
    partition_columns VARCHAR(500)[], -- Array of column names
    
    -- Schema
    schema_json TEXT,
    row_count BIGINT,
    size_bytes BIGINT,
    
    -- Metadata
    description TEXT,
    tags VARCHAR(100)[],
    created_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP,
    
    UNIQUE(tenant_id, database_name, table_name)
);

CREATE INDEX idx_datasets_tenant_id ON datasets(tenant_id);
CREATE INDEX idx_datasets_database ON datasets(database_name);
CREATE INDEX idx_datasets_format ON datasets(format);

-- Spark Jobs (execution history)
CREATE TABLE IF NOT EXISTS spark_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(100) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    cluster_id BIGINT REFERENCES spark_clusters(id),
    notebook_id BIGINT REFERENCES notebooks(id),
    
    -- Job details
    job_type VARCHAR(50) NOT NULL, -- NOTEBOOK, SQL, JAR, PYTHON
    job_name VARCHAR(200),
    spark_app_id VARCHAR(200),
    status VARCHAR(50) NOT NULL, -- SUBMITTED, RUNNING, SUCCEEDED, FAILED, CANCELLED
    
    -- Execution
    submitted_by_user_id BIGINT REFERENCES users(id),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Results
    output_location VARCHAR(1000),
    error_message TEXT,
    logs_path VARCHAR(1000)
);

CREATE INDEX idx_jobs_tenant_id ON spark_jobs(tenant_id);
CREATE INDEX idx_jobs_cluster_id ON spark_jobs(cluster_id);
CREATE INDEX idx_jobs_status ON spark_jobs(status);
CREATE INDEX idx_jobs_notebook_id ON spark_jobs(notebook_id);

-- Workflows (ETL Pipelines)
CREATE TABLE IF NOT EXISTS workflows (
    id BIGSERIAL PRIMARY KEY,
    workflow_id VARCHAR(100) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    
    -- DAG definition
    dag_definition TEXT, -- JSON format
    schedule_cron VARCHAR(100),
    enabled BOOLEAN DEFAULT TRUE,
    
    -- Execution
    last_run_at TIMESTAMP,
    next_run_at TIMESTAMP,
    run_count INTEGER DEFAULT 0,
    
    -- Metadata
    created_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workflows_tenant_id ON workflows(tenant_id);
CREATE INDEX idx_workflows_enabled ON workflows(enabled);

-- ML Models
CREATE TABLE IF NOT EXISTS ml_models (
    id BIGSERIAL PRIMARY KEY,
    model_id VARCHAR(100) UNIQUE NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    version VARCHAR(50) NOT NULL,
    
    -- Model details
    framework VARCHAR(50), -- SPARK_ML, SKLEARN, TENSORFLOW, PYTORCH
    algorithm VARCHAR(100),
    stage VARCHAR(20) DEFAULT 'DEVELOPMENT', -- DEVELOPMENT, STAGING, PRODUCTION
    
    -- Artifacts
    artifact_path VARCHAR(1000),
    metrics_json TEXT,
    params_json TEXT,
    
    -- Lineage
    notebook_id BIGINT REFERENCES notebooks(id),
    training_dataset_id BIGINT REFERENCES datasets(id),
    
    -- Metadata
    created_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deployed_at TIMESTAMP,
    
    UNIQUE(tenant_id, name, version)
);

CREATE INDEX idx_models_tenant_id ON ml_models(tenant_id);
CREATE INDEX idx_models_stage ON ml_models(stage);
CREATE INDEX idx_models_framework ON ml_models(framework);

