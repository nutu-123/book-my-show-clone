import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import axiosInstance from '../../utils/axiosInstance'

export const fetchShows = createAsyncThunk(
  'shows/fetch',
  async ({ movieId, city, date }, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get(
        `/shows?movieId=${movieId}&city=${city}&date=${date}`
      )
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const fetchShowById = createAsyncThunk(
  'shows/fetchById',
  async (showId, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get(`/shows/${showId}`)
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const fetchCities = createAsyncThunk(
  'shows/cities',
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get('/theatres/cities')
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

const showSlice = createSlice({
  name: 'shows',
  initialState: {
    list:       [],
    current:    null,
    cities:     [],
    loading:    false,
    error:      null,
    selectedCity: localStorage.getItem('selectedCity') || 'Mumbai',
    selectedDate: new Date().toISOString().split('T')[0]
  },
  reducers: {
    setSelectedCity: (s, a) => {
      s.selectedCity = a.payload
      localStorage.setItem('selectedCity', a.payload)
    },
    setSelectedDate: (s, a) => { s.selectedDate = a.payload },
    clearCurrentShow:(s)    => { s.current = null }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchShows.pending,   (s) => { s.loading = true })
      .addCase(fetchShows.fulfilled, (s, a) => {
        s.loading = false; s.list = a.payload || []
      })
      .addCase(fetchShows.rejected,  (s, a) => {
        s.loading = false; s.error = a.payload
      })

      .addCase(fetchShowById.pending,   (s) => { s.loading = true })
      .addCase(fetchShowById.fulfilled, (s, a) => {
        s.loading = false; s.current = a.payload
      })
      .addCase(fetchShowById.rejected,  (s, a) => {
        s.loading = false; s.error = a.payload
      })

      .addCase(fetchCities.fulfilled, (s, a) => {
        s.cities = a.payload || []
      })
  }
})

export const { setSelectedCity, setSelectedDate, clearCurrentShow } =
  showSlice.actions
export default showSlice.reducer