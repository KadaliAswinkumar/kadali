# Kadali Dashboard - React Frontend

Modern, responsive dashboard for the Kadali Data Platform.

## Features

- 📊 **Dashboard Overview** - System stats and recent activity
- 🖥️ **Cluster Management** - Create and manage Spark clusters
- 📓 **Notebooks** - Interactive data analysis
- 🗄️ **Data Explorer** - SQL query interface and data catalog
- 🧠 **ML Models** - Model registry and deployment
- ⚙️ **Settings** - Tenant configuration

## Tech Stack

- **React 18** with TypeScript
- **Material-UI** for components
- **Vite** for blazing fast builds
- **React Query** for data fetching
- **Axios** for API calls
- **React Router** for navigation

## Quick Start

### Development

```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Access at http://localhost:3000
```

### Production Build

```bash
# Build for production
npm run build

# Preview production build
npm run preview
```

### Docker Build

```bash
# Build Docker image
docker build -t kadali-frontend:latest .

# Run container
docker run -p 8081:80 kadali-frontend:latest
```

## Configuration

Create `.env` file in the frontend directory:

```env
VITE_API_URL=https://api.kadali.yourdomain.com/api/v1
```

For local development, the API proxy is configured in `vite.config.ts`.

## Deployment to Kubernetes

```bash
# Build and push image
docker build -t your-registry/kadali-frontend:latest .
docker push your-registry/kadali-frontend:latest

# Create deployment
kubectl apply -f k8s/frontend-deployment.yaml
```

## Project Structure

```
frontend/
├── src/
│   ├── api/           # API client and endpoints
│   ├── components/    # Reusable components
│   ├── pages/         # Page components
│   ├── App.tsx        # Main app component
│   └── main.tsx       # Entry point
├── public/            # Static assets
├── Dockerfile         # Production Docker build
└── vite.config.ts     # Vite configuration
```

## Available Pages

- `/` - Dashboard overview
- `/clusters` - Spark cluster management
- `/notebooks` - Jupyter notebooks
- `/data` - Data explorer and SQL editor
- `/models` - ML model registry
- `/settings` - Configuration

## API Integration

The dashboard integrates with Kadali backend APIs:

```typescript
// Example: Create cluster
import { clustersApi } from './api/clusters'

const cluster = await clustersApi.create({
  name: 'my-cluster',
  type: 'INTERACTIVE',
  driverMemory: '2g',
  driverCores: 1,
  executorMemory: '2g',
  executorCores: 1,
  executorCount: 2
})
```

## Customization

### Theme

Edit `src/main.tsx` to customize the Material-UI theme:

```typescript
const theme = createTheme({
  palette: {
    primary: { main: '#your-color' },
    secondary: { main: '#your-color' },
  },
})
```

### Branding

- Update `index.html` title
- Replace favicon in `public/`
- Modify logo in `src/components/Layout.tsx`

## Browser Support

- Chrome/Edge (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)

## Performance

- Code splitting with React.lazy()
- Image optimization
- Gzip compression
- Asset caching
- Tree shaking

## Security

- CORS configured
- XSS protection
- CSRF tokens (if needed)
- Secure headers via nginx

## Contributing

1. Create feature branch
2. Make changes
3. Test thoroughly
4. Submit pull request

## License

Proprietary - Kadali Data Platform

---

**Built with ❤️ for data teams**

