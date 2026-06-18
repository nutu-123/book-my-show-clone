import { configureStore } from '@reduxjs/toolkit'
import authReducer    from './slices/authSlice'
import movieReducer   from './slices/movieSlice'
import showReducer    from './slices/showSlice'
import bookingReducer from './slices/bookingSlice'
import paymentReducer from './slices/paymentSlice'

export const store = configureStore({
  reducer: {
    auth:    authReducer,
    movies:  movieReducer,
    shows:   showReducer,
    booking: bookingReducer,
    payment: paymentReducer
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({ serializableCheck: false })
})