import { useState } from 'react'
import { useQuery, useMutation } from 'react-query'
import {
  Box,
  Button,
  Typography,
  Paper,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  Tab,
} from '@mui/material'
import { PlayArrow } from '@mui/icons-material'
import { dataApi } from '../api/data'
import { toast } from 'react-toastify'

export default function DataExplorer() {
  const [tab, setTab] = useState(0)
  const [sql, setSql] = useState('SELECT * FROM analytics.users LIMIT 10')
  const [queryResult, setQueryResult] = useState<any>(null)

  const { data: databases } = useQuery('databases', () => dataApi.listDatabases())
  const { data: datasets } = useQuery('datasets', () => dataApi.listDatasets())

  const executeMutation = useMutation(
    (sql: string) => dataApi.executeQuery(sql),
    {
      onSuccess: (response) => {
        setQueryResult(response.data)
        if (response.data.status === 'COMPLETED') {
          toast.success(`Query completed! ${response.data.rowCount} rows returned`)
        } else if (response.data.status === 'FAILED') {
          toast.error(`Query failed: ${response.data.errorMessage}`)
        }
      },
      onError: () => {
        toast.error('Query execution failed')
      },
    }
  )

  const handleExecute = () => {
    executeMutation.mutate(sql)
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Data Explorer
      </Typography>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="SQL Editor" />
        <Tab label="Databases" />
        <Tab label="Tables" />
      </Tabs>

      {tab === 0 && (
        <Box>
          <Paper sx={{ p: 2, mb: 2 }}>
            <TextField
              fullWidth
              multiline
              rows={6}
              label="SQL Query"
              value={sql}
              onChange={(e) => setSql(e.target.value)}
              variant="outlined"
              sx={{ fontFamily: 'monospace' }}
            />
            <Box mt={2}>
              <Button
                variant="contained"
                startIcon={<PlayArrow />}
                onClick={handleExecute}
                disabled={executeMutation.isLoading}
              >
                {executeMutation.isLoading ? 'Executing...' : 'Execute'}
              </Button>
            </Box>
          </Paper>

          {queryResult && (
            <Paper sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Results ({queryResult.rowCount} rows)
              </Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      {queryResult.columns?.map((col: string) => (
                        <TableCell key={col}>{col}</TableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {queryResult.data?.map((row: any, idx: number) => (
                      <TableRow key={idx}>
                        {queryResult.columns?.map((col: string) => (
                          <TableCell key={col}>{String(row[col])}</TableCell>
                        ))}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Paper>
          )}
        </Box>
      )}

      {tab === 1 && (
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Databases
          </Typography>
          {databases?.data.map((db) => (
            <Box key={db} sx={{ p: 1 }}>
              • {db}
            </Box>
          ))}
        </Paper>
      )}

      {tab === 2 && (
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>
            Tables
          </Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Database</TableCell>
                  <TableCell>Table</TableCell>
                  <TableCell>Format</TableCell>
                  <TableCell>Rows</TableCell>
                  <TableCell>Size</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {datasets?.data.map((dataset) => (
                  <TableRow key={dataset.datasetId}>
                    <TableCell>{dataset.databaseName}</TableCell>
                    <TableCell>{dataset.tableName}</TableCell>
                    <TableCell>{dataset.format}</TableCell>
                    <TableCell>{dataset.rowCount?.toLocaleString()}</TableCell>
                    <TableCell>
                      {(dataset.sizeBytes / 1024 / 1024).toFixed(2)} MB
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}
    </Box>
  )
}

