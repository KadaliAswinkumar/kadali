import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import {
  Box,
  Button,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
} from '@mui/material'
import { Delete, Add, Refresh } from '@mui/icons-material'
import { clustersApi, CreateClusterRequest } from '../api/clusters'
import { toast } from 'react-toastify'

const statusColors: Record<string, any> = {
  RUNNING: 'success',
  CREATING: 'info',
  TERMINATED: 'default',
  ERROR: 'error',
}

export default function Clusters() {
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState<CreateClusterRequest>({
    name: '',
    type: 'INTERACTIVE',
    driverMemory: '2g',
    driverCores: 1,
    executorMemory: '2g',
    executorCores: 1,
    executorCount: 2,
  })

  const queryClient = useQueryClient()
  const { data, isLoading, refetch } = useQuery('clusters', () => clustersApi.list())

  const createMutation = useMutation(clustersApi.create, {
    onSuccess: () => {
      toast.success('Cluster created successfully!')
      setOpen(false)
      queryClient.invalidateQueries('clusters')
    },
    onError: () => {
      toast.error('Failed to create cluster')
    },
  })

  const deleteMutation = useMutation(clustersApi.terminate, {
    onSuccess: () => {
      toast.success('Cluster terminated')
      queryClient.invalidateQueries('clusters')
    },
  })

  const handleCreate = () => {
    createMutation.mutate(form)
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Spark Clusters</Typography>
        <Box>
          <IconButton onClick={() => refetch()}>
            <Refresh />
          </IconButton>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setOpen(true)}
          >
            Create Cluster
          </Button>
        </Box>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Resources</TableCell>
              <TableCell>Created</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  Loading...
                </TableCell>
              </TableRow>
            ) : data?.data.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  No clusters found. Create your first cluster!
                </TableCell>
              </TableRow>
            ) : (
              data?.data.map((cluster) => (
                <TableRow key={cluster.clusterId}>
                  <TableCell>{cluster.name}</TableCell>
                  <TableCell>{cluster.type}</TableCell>
                  <TableCell>
                    <Chip
                      label={cluster.status}
                      color={statusColors[cluster.status]}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {cluster.executorCount} executors ({cluster.executorCores} cores, {cluster.executorMemory} each)
                  </TableCell>
                  <TableCell>
                    {new Date(cluster.createdAt).toLocaleString()}
                  </TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      onClick={() => deleteMutation.mutate(cluster.clusterId)}
                      disabled={cluster.status === 'TERMINATED'}
                    >
                      <Delete />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Cluster</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            margin="normal"
            label="Cluster Name"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
          <TextField
            fullWidth
            margin="normal"
            select
            label="Type"
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value as any })}
          >
            <MenuItem value="INTERACTIVE">Interactive</MenuItem>
            <MenuItem value="JOB">Job</MenuItem>
            <MenuItem value="ML">ML</MenuItem>
          </TextField>
          <TextField
            fullWidth
            margin="normal"
            label="Driver Memory"
            value={form.driverMemory}
            onChange={(e) => setForm({ ...form, driverMemory: e.target.value })}
          />
          <TextField
            fullWidth
            margin="normal"
            type="number"
            label="Driver Cores"
            value={form.driverCores}
            onChange={(e) => setForm({ ...form, driverCores: Number(e.target.value) })}
          />
          <TextField
            fullWidth
            margin="normal"
            label="Executor Memory"
            value={form.executorMemory}
            onChange={(e) => setForm({ ...form, executorMemory: e.target.value })}
          />
          <TextField
            fullWidth
            margin="normal"
            type="number"
            label="Executor Cores"
            value={form.executorCores}
            onChange={(e) => setForm({ ...form, executorCores: Number(e.target.value) })}
          />
          <TextField
            fullWidth
            margin="normal"
            type="number"
            label="Executor Count"
            value={form.executorCount}
            onChange={(e) => setForm({ ...form, executorCount: Number(e.target.value) })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button onClick={handleCreate} variant="contained">
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

