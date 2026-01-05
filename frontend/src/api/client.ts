import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
    'X-Tenant-ID': localStorage.getItem('tenantId') || 'default-tenant'
  }
})

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    const tenantId = localStorage.getItem('tenantId')
    if (tenantId && config.headers) {
      config.headers['X-Tenant-ID'] = tenantId
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export default apiClient

