import { useQuery } from 'react-query'
import {
  Grid,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
} from '@mui/material'
import {
  Computer,
  Description,
  Storage,
  Psychology,
} from '@mui/icons-material'
import { clustersApi } from '../api/clusters'
import { dataApi } from '../api/data'

const StatCard = ({ title, value, icon, color }: any) => (
  <Card>
    <CardContent>
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Box>
          <Typography color="textSecondary" gutterBottom>
            {title}
          </Typography>
          <Typography variant="h4">{value}</Typography>
        </Box>
        <Box sx={{ color, fontSize: 48 }}>
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
)

export default function Dashboard() {
  const { data: clusters } = useQuery('clusters', () => clustersApi.list())
  const { data: databases } = useQuery('databases', () => dataApi.listDatabases())

  const runningClusters = clusters?.data.filter(c => c.status === 'RUNNING').length || 0
  const totalDatabases = databases?.data.length || 0

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard Overview
      </Typography>

      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Running Clusters"
            value={runningClusters}
            icon={<Computer />}
            color="#1976d2"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Notebooks"
            value={0}
            icon={<Description />}
            color="#2e7d32"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Databases"
            value={totalDatabases}
            icon={<Storage />}
            color="#ed6c02"
          />
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="ML Models"
            value={0}
            icon={<Psychology />}
            color="#9c27b0"
          />
        </Grid>
      </Grid>

      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Recent Clusters
            </Typography>
            {clusters?.data.slice(0, 5).map((cluster) => (
              <Box key={cluster.clusterId} sx={{ mb: 2 }}>
                <Typography variant="subtitle1">{cluster.name}</Typography>
                <Typography variant="body2" color="textSecondary">
                  Status: {cluster.status} | Type: {cluster.type}
                </Typography>
              </Box>
            ))}
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Quick Actions
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <Typography variant="body1">
                • Create a new Spark cluster
              </Typography>
              <Typography variant="body1">
                • Launch a notebook
              </Typography>
              <Typography variant="body1">
                • Execute SQL query
              </Typography>
              <Typography variant="body1">
                • Upload data
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  )
}

