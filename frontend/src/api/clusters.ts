import apiClient from './client'

export interface Cluster {
  clusterId: string
  name: string
  type: string
  status: string
  driverMemory: string
  driverCores: number
  executorMemory: string
  executorCores: number
  executorCount: number
  sparkUiUrl?: string
  createdAt: string
  startedAt?: string
  lastActivityAt?: string
}

export interface CreateClusterRequest {
  name: string
  type: 'INTERACTIVE' | 'JOB' | 'ML'
  driverMemory: string
  driverCores: number
  executorMemory: string
  executorCores: number
  executorCount: number
}

export const clustersApi = {
  list: () => apiClient.get<Cluster[]>('/clusters'),
  
  get: (clusterId: string) => apiClient.get<Cluster>(`/clusters/${clusterId}`),
  
  create: (data: CreateClusterRequest) => apiClient.post<Cluster>('/clusters', data),
  
  terminate: (clusterId: string) => apiClient.delete(`/clusters/${clusterId}`),
  
  updateActivity: (clusterId: string) => apiClient.post(`/clusters/${clusterId}/activity`)
}

