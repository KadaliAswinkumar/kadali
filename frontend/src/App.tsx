import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import Clusters from './pages/Clusters'
import Notebooks from './pages/Notebooks'
import DataExplorer from './pages/DataExplorer'
import Models from './pages/Models'
import Settings from './pages/Settings'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/clusters" element={<Clusters />} />
        <Route path="/notebooks" element={<Notebooks />} />
        <Route path="/data" element={<DataExplorer />} />
        <Route path="/models" element={<Models />} />
        <Route path="/settings" element={<Settings />} />
      </Routes>
    </Layout>
  )
}

export default App

