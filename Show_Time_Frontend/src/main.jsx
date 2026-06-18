import React from 'react'
import ReactDOM from 'react-dom/client'
import { Provider } from 'react-redux'
import { Toaster } from 'react-hot-toast'
import App from './App.jsx'
import { store } from './store/store.js'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={store}>
      <App />
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#1a1a2e',
            color: '#ffffff',
            border: '1px solid #2a2a4a',
            borderRadius: '10px',
          },
          success: {
            iconTheme: { primary: '#4caf50', secondary: '#fff' }
          },
          error: {
            iconTheme: { primary: '#e50914', secondary: '#fff' }
          }
        }}
      />
    </Provider>
  </React.StrictMode>
)