import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import axiosInstance from '../../utils/axiosInstance'
import toast from 'react-hot-toast'

export const lockSeats = createAsyncThunk(
  'booking/lockSeats',
  async ({ showId, seatNumbers }, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.post(
        '/bookings/lock-seats',
        { showId, seatNumbers }
      )
      return data.data
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Failed to lock seats'
      )
    }
  }
)

export const createBooking = createAsyncThunk(
  'booking/create',
  async (bookingData, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.post(
        '/bookings', bookingData
      )
      return data.data
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Booking failed'
      )
    }
  }
)

export const fetchUserBookings = createAsyncThunk(
  'booking/userBookings',
  async ({ page = 0, size = 10 } = {}, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get(
        `/bookings/user?page=${page}&size=${size}`
      )
      return data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const fetchBookingById = createAsyncThunk(
  'booking/fetchById',
  async (id, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get(`/bookings/${id}`)
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const cancelBooking = createAsyncThunk(
  'booking/cancel',
  async ({ id, reason }, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.put(
        `/bookings/${id}/cancel?reason=${reason}`
      )
      toast.success('Booking cancelled successfully')
      return data.data
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Cancellation failed'
      )
    }
  }
)

const bookingSlice = createSlice({
  name: 'booking',
  initialState: {
    current:      null,
    lockInfo:     null,
    userBookings: [],
    totalPages:   0,
    loading:      false,
    lockLoading:  false,
    error:        null,
    selectedSeats:[]
  },
  reducers: {
    setSelectedSeats:(s, a) => { s.selectedSeats = a.payload },
    clearBooking:    (s)    => {
      s.current = null; s.lockInfo = null; s.selectedSeats = []
    },
    clearError: (s) => { s.error = null }
  },
  extraReducers: (builder) => {
    builder
      .addCase(lockSeats.pending,   (s) => { s.lockLoading = true; s.error = null })
      .addCase(lockSeats.fulfilled, (s, a) => {
        s.lockLoading = false; s.lockInfo = a.payload
        toast.success('Seats locked! Complete payment within 10 minutes.')
      })
      .addCase(lockSeats.rejected,  (s, a) => {
        s.lockLoading = false; s.error = a.payload
        toast.error(a.payload || 'Selected seats are not available')
      })

      .addCase(createBooking.pending,   (s) => { s.loading = true })
      .addCase(createBooking.fulfilled, (s, a) => {
        s.loading = false; s.current = a.payload
      })
      .addCase(createBooking.rejected,  (s, a) => {
        s.loading = false; s.error = a.payload
        toast.error(a.payload)
      })

      .addCase(fetchUserBookings.pending,   (s) => { s.loading = true })
      .addCase(fetchUserBookings.fulfilled, (s, a) => {
        s.loading      = false
        s.userBookings = a.payload.data?.content || []
        s.totalPages   = a.payload.totalPages   || 0
      })
      .addCase(fetchUserBookings.rejected,  (s, a) => {
        s.loading = false; s.error = a.payload
      })

      .addCase(fetchBookingById.pending,   (s) => { s.loading = true })
      .addCase(fetchBookingById.fulfilled, (s, a) => {
        s.loading = false; s.current = a.payload
      })
      .addCase(fetchBookingById.rejected,  (s, a) => {
        s.loading = false; s.error = a.payload
      })

      .addCase(cancelBooking.fulfilled, (s, a) => {
        s.current = a.payload
        s.userBookings = s.userBookings.map(b =>
          b.id === a.payload.id ? a.payload : b
        )
      })
  }
})

export const { setSelectedSeats, clearBooking, clearError } =
  bookingSlice.actions
export default bookingSlice.reducer