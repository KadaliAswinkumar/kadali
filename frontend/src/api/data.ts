import apiClient from './client'

export interface Dataset {
  datasetId: string
  databaseName: string
  tableName: string
  format: string
  rowCount: number
  sizeBytes: number
  createdAt: string
}

export interface QueryResult {
  queryId: string
  sql: string
  status: string
  columns: string[]
  data: Record<string, any>[]
  rowCount: number
  startTime: string
  endTime?: string
  errorMessage?: string
}

export const dataApi = {
  listDatabases: () => apiClient.get<string[]>('/data/databases'),
  
  createDatabase: (databaseName: string) => 
    apiClient.post('/data/databases', null, { params: { databaseName } }),
  
  listDatasets: (database?: string) => 
    apiClient.get<Dataset[]>('/data/datasets', { params: { database } }),
  
  getDataset: (database: string, table: string) => 
    apiClient.get<Dataset>(`/data/datasets/${database}/${table}`),
  
  deleteDataset: (database: string, table: string) => 
    apiClient.delete(`/data/datasets/${database}/${table}`),
  
  executeQuery: (sql: string, limit: number = 1000) => 
    apiClient.post<QueryResult>('/data/query', { sql, limit }),
  
  getQueryResult: (queryId: string) => 
    apiClient.get<QueryResult>(`/data/query/${queryId}`),
  
  cancelQuery: (queryId: string) => 
    apiClient.delete(`/data/query/${queryId}`)
}

