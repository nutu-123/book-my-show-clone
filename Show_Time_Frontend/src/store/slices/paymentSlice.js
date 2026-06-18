import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import axiosInstance from '../../utils/axiosInstance'
import toast from 'react-hot-toast'

export const initiatePayment = createAsyncThunk(
  'payment/initiate',
  async (paymentData, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.post(
        '/payments/initiate', paymentData
      )
      return data.data
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Payment initiation failed'
      )
    }
  }
)

export const verifyPayment = createAsyncThunk(
  'payment/verify',
  async (verifyData, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.post(
        '/payments/verify', verifyData
      )
      return data.data
    } catch (err) {
      return rejectWithValue(
        err.response?.data?.message || 'Payment verification failed'
      )
    }
  }
)

const paymentSlice = createSlice({
  name: 'payment',
  initialState: {
    current: null,
    loading: false,
    error:   null
  },
  reducers: {
    clearPayment: (s) => { s.current = null; s.error = null }
  },
  extraReducers: (builder) => {
    builder
      .addCase(initiatePayment.pending,   (s) => { s.loading = true; s.error = null })
      .addCase(initiatePayment.fulfilled, (s, a) => {
        s.loading = false; s.current = a.payload
      })
      .addCase(initiatePayment.rejected,  (s, a) => {
        s.loading = false; s.error = a.payload
        toast.error(a.payload)
      })

      .addCase(verifyPayment.pending,   (s) => { s.loading = true })
      .addCase(verifyPayment.fulfilled, (s, a) => {
        s.loading = false; s.current = a.payload
        if (a.payload.status === 'SUCCESS') {
          toast.success('Payment successful! Booking confirmed 🎉')
        } else {
          toast.error('Payment failed. Please try again.')
        }
      })
      .addCase(verifyPayment.rejected,  (s, a) => {
        s.loading = false; s.error = a.payload
        toast.error(a.payload)
      })
  }
})

export const { clearPayment } = paymentSlice.actions
export default paymentSlice.reducer