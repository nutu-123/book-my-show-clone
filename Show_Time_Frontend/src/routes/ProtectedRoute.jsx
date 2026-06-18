import { Navigate, Outlet } from 'react-router-dom'
import { useSelector } from 'react-redux'
import toast from 'react-hot-toast'

export default function ProtectedRoute({ adminOnly = false }) {
  const { isAuthenticated, user } = useSelector((s) => s.auth)

  if (!isAuthenticated) {
    toast.error('Please login to continue')
    return <Navigate to="/login" replace />
  }

  if (adminOnly && !user?.roles?.includes('ROLE_ADMIN')) {
    toast.error('Admin access required')
    return <Navigate to="/" replace />
  }

  return <Outlet />
}