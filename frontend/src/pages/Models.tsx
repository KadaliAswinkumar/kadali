import { Box, Typography, Button, Paper } from '@mui/material'
import { Add } from '@mui/icons-material'

export default function Models() {
  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">ML Models</Typography>
        <Button variant="contained" startIcon={<Add />}>
          Register Model
        </Button>
      </Box>

      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Typography variant="h6" gutterBottom>
          No models registered
        </Typography>
        <Typography color="textSecondary">
          Register your first ML model to track versions and deploy to production
        </Typography>
      </Paper>
    </Box>
  )
}

