import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { loginUser, clearError } from '../store/slices/authSlice'
import { FiMail, FiLock, FiEye, FiEyeOff, FiFilm } from 'react-icons/fi'

export default function Login() {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()
  const { loading, error } = useSelector((s) => s.auth)

  const [form, setForm]     = useState({ email:'', password:'' })
  const [showPwd, setShowPwd] = useState(false)

  useEffect(() => { return () => dispatch(clearError()) }, [dispatch])

  const handleSubmit = async (e) => {
    e.preventDefault()
    const result = await dispatch(loginUser(form))
    if (!result.error) navigate('/')
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        {/* Header */}
        <div style={styles.header}>
          <FiFilm size={36} color="#e50914" />
          <h1 style={styles.title}>Welcome Back</h1>
          <p style={styles.subtitle}>Sign in to your ShowTime account</p>
        </div>

        {/* Demo credentials */}
        <div style={styles.demo}>
          <p style={{ fontSize:12, color:'#a0a0a0', marginBottom:8 }}>
            🔑 Demo Credentials
          </p>
          <div style={{ display:'flex', gap:8 }}>
            <button
              style={styles.demoBtn}
              onClick={() => setForm({
                email:'test@showtime.com', password:'Test@1234'
              })}
            >
              User Login
            </button>
            <button
              style={styles.demoBtn}
              onClick={() => setForm({
                email:'admin@showtime.com', password:'Admin@123'
              })}
            >
              Admin Login
            </button>
          </div>
        </div>

        {/* Error */}
        {error && (
          <div style={styles.error}>❌ {error}</div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email Address</label>
            <div style={styles.inputWrap}>
              <FiMail size={16} color="#a0a0a0" style={styles.inputIcon} />
              <input
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={(e) => setForm({ ...form, email:e.target.value })}
                required
                style={styles.input}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Password</label>
            <div style={styles.inputWrap}>
              <FiLock size={16} color="#a0a0a0" style={styles.inputIcon} />
              <input
                type={showPwd ? 'text' : 'password'}
                placeholder="Enter password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password:e.target.value })}
                required
                style={{ ...styles.input, paddingRight:40 }}
              />
              <button
                type="button"
                onClick={() => setShowPwd(!showPwd)}
                style={styles.eyeBtn}
              >
                {showPwd ? <FiEyeOff size={16} /> : <FiEye size={16} />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-lg"
            style={{ width:'100%', marginTop:8, justifyContent:'center' }}
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p style={styles.footer}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color:'#e50914', fontWeight:600 }}>
            Create one free
          </Link>
        </p>
      </div>
    </div>
  )
}

const styles = {
  page: {
    minHeight:'calc(100vh - 64px)',
    display:'flex', alignItems:'center', justifyContent:'center',
    padding:'40px 20px'
  },
  card: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:16, padding:'40px 36px',
    width:'100%', maxWidth:420,
    boxShadow:'0 20px 60px rgba(0,0,0,0.5)'
  },
  header: { textAlign:'center', marginBottom:24 },
  title: { fontSize:26, fontWeight:800, marginTop:12 },
  subtitle: { color:'#a0a0a0', fontSize:14, marginTop:6 },
  demo: {
    background:'#16213e', borderRadius:10, padding:'12px 16px',
    marginBottom:24, border:'1px solid #2a2a4a'
  },
  demoBtn: {
    background:'#1a1a2e', border:'1px solid #e50914',
    color:'#e50914', borderRadius:6, padding:'6px 14px',
    fontSize:12, fontWeight:600, cursor:'pointer'
  },
  error: {
    background:'rgba(244,67,54,0.1)', border:'1px solid rgba(244,67,54,0.3)',
    borderRadius:8, padding:'12px 16px',
    color:'#f44336', fontSize:13, marginBottom:16
  },
  inputWrap: { position:'relative' },
  inputIcon: { position:'absolute', left:12, top:'50%', transform:'translateY(-50%)' },
  input: {
    width:'100%', background:'#16213e', border:'1px solid #2a2a4a',
    borderRadius:10, padding:'12px 12px 12px 40px',
    color:'#fff', fontSize:14
  },
  eyeBtn: {
    position:'absolute', right:12, top:'50%', transform:'translateY(-50%)',
    background:'transparent', color:'#a0a0a0', cursor:'pointer'
  },
  footer: { textAlign:'center', color:'#a0a0a0', fontSize:14, marginTop:24 }
}