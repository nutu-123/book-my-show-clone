// src/components/common/Loader.jsx
import React from 'react'

export default function Loader() {
  return (
    <div style={styles.loaderContainer}>
      <div style={styles.spinner}></div>
      <p style={styles.text}>Loading...</p>
    </div>
  )
}

const styles = {
  loaderContainer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '40px',
    minHeight: '200px'
  },
  spinner: {
    width: '48px',
    height: '48px',
    border: '4px solid rgba(229, 9, 20, 0.1)',
    borderTop: '4px solid #e50914',
    borderRight: '4px solid #e50914',
    borderRadius: '50%',
    animation: 'spin 0.8s linear infinite'
  },
  text: {
    marginTop: '16px',
    color: '#a0a0a0',
    fontSize: '14px'
  }
}

// Add keyframes for animation - put this in your global CSS file instead
// If you have a global CSS file, add:
// @keyframes spin {
//   0% { transform: rotate(0deg); }
//   100% { transform: rotate(360deg); }
// }