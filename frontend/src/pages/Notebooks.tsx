import { Box, Typography, Button, Paper } from '@mui/material'
import { Add } from '@mui/icons-material'

export default function Notebooks() {
  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Notebooks</Typography>
        <Button variant="contained" startIcon={<Add />}>
          New Notebook
        </Button>
      </Box>

      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Typography variant="h6" gutterBottom>
          No notebooks yet
        </Typography>
        <Typography color="textSecondary">
          Create your first notebook to start analyzing data with Python or SQL
        </Typography>
      </Paper>
    </Box>
  )
}

