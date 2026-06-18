import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import axiosInstance from '../../utils/axiosInstance'

export const fetchMovies = createAsyncThunk(
  'movies/fetchAll',
  async ({ page = 0, size = 12 } = {}, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get(
        `/movies?page=${page}&size=${size}`
      )
      return data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const fetchTrending = createAsyncThunk(
  'movies/trending',
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get('/movies/trending')
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const fetchMovieById = createAsyncThunk(
  'movies/fetchById',
  async (id, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get(`/movies/${id}`)
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const searchMovies = createAsyncThunk(
  'movies/search',
  async ({ q, page = 0, size = 12 }, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get(
        `/movies/search?q=${q}&page=${page}&size=${size}`
      )
      return data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

export const fetchGenres = createAsyncThunk(
  'movies/genres',
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await axiosInstance.get('/movies/genres')
      return data.data
    } catch (err) {
      return rejectWithValue(err.response?.data?.message)
    }
  }
)

const movieSlice = createSlice({
  name: 'movies',
  initialState: {
    list:         [],
    trending:     [],
    current:      null,
    genres:       [],
    totalPages:   0,
    totalElements:0,
    currentPage:  0,
    loading:      false,
    error:        null
  },
  reducers: {
    clearCurrentMovie: (state) => { state.current = null }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMovies.pending,  (s) => { s.loading = true })
      .addCase(fetchMovies.fulfilled,(s, a) => {
        s.loading       = false
        s.list          = a.payload.data?.content || []
        s.totalPages    = a.payload.totalPages    || 0
        s.totalElements = a.payload.totalElements || 0
        s.currentPage   = a.payload.page          || 0
      })
      .addCase(fetchMovies.rejected, (s, a) => {
        s.loading = false; s.error = a.payload
      })

      .addCase(fetchTrending.fulfilled,(s, a) => { s.trending = a.payload || [] })

      .addCase(fetchMovieById.pending,  (s) => { s.loading = true })
      .addCase(fetchMovieById.fulfilled,(s, a) => {
        s.loading = false; s.current = a.payload
      })
      .addCase(fetchMovieById.rejected, (s, a) => {
        s.loading = false; s.error = a.payload
      })

      .addCase(searchMovies.pending,  (s) => { s.loading = true })
      .addCase(searchMovies.fulfilled,(s, a) => {
        s.loading = false
        s.list    = a.payload.data?.content || []
        s.totalPages = a.payload.totalPages || 0
      })
      .addCase(searchMovies.rejected, (s, a) => {
        s.loading = false; s.error = a.payload
      })

      .addCase(fetchGenres.fulfilled,(s, a) => { s.genres = a.payload || [] })
  }
})

export const { clearCurrentMovie } = movieSlice.actions
export default movieSlice.reducer