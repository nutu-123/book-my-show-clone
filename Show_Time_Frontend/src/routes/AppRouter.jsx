import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useSelector } from 'react-redux'
import MainLayout from '../components/layout/MainLayout'
import ProtectedRoute from './ProtectedRoute'
import Home from '../pages/Home'
import Login from '../pages/Login'
import Register from '../pages/Register'
import MovieListing from '../pages/MovieListing'
import MovieDetails from '../pages/MovieDetails'
import TheatreSelection from '../pages/TheatreSelection'
import SeatSelection from '../pages/SeatSelection'
import PaymentPage from '../pages/PaymentPage'
import BookingConfirmation from '../pages/BookingConfirmation'
import MyBookings from '../pages/MyBookings'
import Profile from '../pages/Profile'
import AdminDashboard from '../pages/AdminDashboard'

export default function AppRouter() {
  const { isAuthenticated, user } = useSelector((s) => s.auth)
  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route element={<MainLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/movies" element={<MovieListing />} />
          <Route path="/movies/:id" element={<MovieDetails />} />
          <Route path="/shows" element={<TheatreSelection />} />

          <Route
            path="/login"
            element={
              isAuthenticated
                ? <Navigate to="/" replace />
                : <Login />
            }
          />
          <Route
            path="/register"
            element={
              isAuthenticated
                ? <Navigate to="/" replace />
                : <Register />
            }
          />

          {/* Protected routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/seats/:showId" element={<SeatSelection />} />
            <Route path="/payment" element={<PaymentPage />} />
            <Route path="/booking/confirmation/:id"
              element={<BookingConfirmation />} />
            <Route path="/my-bookings" element={<MyBookings />} />
            <Route path="/profile" element={<Profile />} />
          </Route>

          {/* Admin routes */}
          <Route element={<ProtectedRoute adminOnly />}>
            <Route path="/admin" element={<AdminDashboard />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}