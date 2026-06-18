import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import axiosInstance from '../../utils/axiosInstance'
import toast from 'react-hot-toast'

// ── Load persisted user from localStorage ──
const storedUser = localStorage.getItem('user')
const initialUser = storedUser ? JSON.parse(storedUser) : null

export const loginUser = createAsyncThunk(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.post('/auth/login', credentials)
      return data.data
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Login failed'
      )
    }
  }
)

export const registerUser = createAsyncThunk(
  'auth/register',
  async (userData, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.post('/auth/register', userData)
      return data.data
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Registration failed'
      )
    }
  }
)

export const fetchProfile = createAsyncThunk(
  'auth/profile',
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get('/auth/me')
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user:    initialUser,
    token:   localStorage.getItem('accessToken'),
    loading: false,
    error:   null,
    isAuthenticated: !!localStorage.getItem('accessToken')
  },
  reducers: {
    logout: (state) => {
      state.user            = null
      state.token           = null
      state.isAuthenticated = false
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      toast.success('Logged out successfully')
    },
    clearError: (state) => { state.error = null }
  },
  extraReducers: (builder) => {
    // Login
    builder
      .addCase(loginUser.pending, (state) => {
        state.loading = true
        state.error   = null
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading         = false
        state.user            = action.payload
        state.token           = action.payload.accessToken
        state.isAuthenticated = true
        localStorage.setItem('accessToken',  action.payload.accessToken)
        localStorage.setItem('refreshToken', action.payload.refreshToken)
        localStorage.setItem('user', JSON.stringify(action.payload))
        toast.success(`Welcome back, ${action.payload.name}! 🎬`)
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false
        state.error   = action.payload
      })

    // Register
    builder
      .addCase(registerUser.pending, (state) => {
        state.loading = true
        state.error   = null
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.loading         = false
        state.user            = action.payload
        state.token           = action.payload.accessToken
        state.isAuthenticated = true
        localStorage.setItem('accessToken',  action.payload.accessToken)
        localStorage.setItem('refreshToken', action.payload.refreshToken)
        localStorage.setItem('user', JSON.stringify(action.payload))
        toast.success('Account created! Welcome to ShowTime 🎉')
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.loading = false
        state.error   = action.payload
      })

    // Profile
    builder
      .addCase(fetchProfile.fulfilled, (state, action) => {
        state.user = { ...state.user, ...action.payload }
        localStorage.setItem('user',
          JSON.stringify({ ...state.user, ...action.payload }))
      })
  }
})

export const { logout, clearError } = authSlice.actions
export default authSlice.reducer