import { useState } from 'react'
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
} from '@mui/material'
import { Save } from '@mui/icons-material'
import { toast } from 'react-toastify'

export default function Settings() {
  const [tenantId, setTenantId] = useState(localStorage.getItem('tenantId') || 'default-tenant')

  const handleSave = () => {
    localStorage.setItem('tenantId', tenantId)
    toast.success('Settings saved!')
    window.location.reload()
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Settings
      </Typography>

      <Paper sx={{ p: 3, maxWidth: 600 }}>
        <Typography variant="h6" gutterBottom>
          Tenant Configuration
        </Typography>

        <TextField
          fullWidth
          margin="normal"
          label="Tenant ID"
          value={tenantId}
          onChange={(e) => setTenantId(e.target.value)}
          helperText="Your organization's tenant identifier"
        />

        <Box mt={3}>
          <Button
            variant="contained"
            startIcon={<Save />}
            onClick={handleSave}
          >
            Save Settings
          </Button>
        </Box>
      </Paper>
    </Box>
  )
}

