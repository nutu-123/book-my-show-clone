import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { registerUser, clearError } from '../store/slices/authSlice'
import {
  FiUser, FiMail, FiLock, FiPhone,
  FiEye, FiEyeOff, FiFilm
} from 'react-icons/fi'

export default function Register() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { loading, error } = useSelector((s) => s.auth)

  const [form, setForm] = useState({
    name:'', email:'', password:'', phone:''
  })
  const [showPwd, setShowPwd] = useState(false)
  const [errors, setErrors]   = useState({})

  useEffect(() => { return () => dispatch(clearError()) }, [dispatch])

  const validate = () => {
    const e = {}
    if (!form.name.trim())       e.name = 'Name is required'
    if (!form.email.trim())      e.email = 'Email is required'
    if (form.password.length < 8) e.password = 'Min 8 characters'
    if (!/(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])/
        .test(form.password))
      e.password = 'Must include uppercase, lowercase, digit, special char'
    if (form.phone && !/^[6-9]\d{9}$/.test(form.phone))
      e.phone = 'Invalid Indian mobile number'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    const result = await dispatch(registerUser(form))
    if (!result.error) navigate('/')
  }

  const Field = ({ icon:Icon, name, type='text', placeholder, required }) => (
    <div className="form-group">
      <label style={{ textTransform:'capitalize' }}>{name}</label>
      <div style={{ position:'relative' }}>
        <Icon size={16} color="#a0a0a0"
          style={{ position:'absolute', left:12,
                   top:'50%', transform:'translateY(-50%)' }} />
        <input
          type={name === 'password' ? (showPwd ? 'text' : 'password') : type}
          placeholder={placeholder}
          value={form[name]}
          onChange={(e) => setForm({ ...form, [name]:e.target.value })}
          required={required}
          style={{
            width:'100%', background:'#16213e',
            border:`1px solid ${errors[name] ? '#e50914' : '#2a2a4a'}`,
            borderRadius:10, padding:'12px 12px 12px 40px',
            color:'#fff', fontSize:14
          }}
        />
        {name === 'password' && (
          <button type="button"
            onClick={() => setShowPwd(!showPwd)}
            style={{
              position:'absolute', right:12, top:'50%',
              transform:'translateY(-50%)',
              background:'transparent', color:'#a0a0a0', cursor:'pointer'
            }}
          >
            {showPwd ? <FiEyeOff size={16} /> : <FiEye size={16} />}
          </button>
        )}
      </div>
      {errors[name] && (
        <span style={{ color:'#e50914', fontSize:12 }}>
          {errors[name]}
        </span>
      )}
    </div>
  )

  return (
    <div style={{
      minHeight:'calc(100vh - 64px)',
      display:'flex', alignItems:'center',
      justifyContent:'center', padding:'40px 20px'
    }}>
      <div style={{
        background:'#1a1a2e', border:'1px solid #2a2a4a',
        borderRadius:16, padding:'40px 36px',
        width:'100%', maxWidth:440,
        boxShadow:'0 20px 60px rgba(0,0,0,0.5)'
      }}>
        <div style={{ textAlign:'center', marginBottom:28 }}>
          <FiFilm size={36} color="#e50914" />
          <h1 style={{ fontSize:24, fontWeight:800, marginTop:10 }}>
            Create Account
          </h1>
          <p style={{ color:'#a0a0a0', fontSize:13, marginTop:6 }}>
            Join ShowTime and book your first ticket
          </p>
        </div>

        {error && (
          <div style={{
            background:'rgba(244,67,54,0.1)',
            border:'1px solid rgba(244,67,54,0.3)',
            borderRadius:8, padding:'12px 16px',
            color:'#f44336', fontSize:13, marginBottom:16
          }}>
            ❌ {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <Field icon={FiUser}  name="name"     placeholder="John Doe" required />
          <Field icon={FiMail}  name="email"    type="email"
            placeholder="you@example.com" required />
          <Field icon={FiLock}  name="password" placeholder="Strong password" required />
          <Field icon={FiPhone} name="phone"    type="tel"
            placeholder="9876543210 (optional)" />

          <button
            type="submit"
            className="btn btn-primary btn-lg"
            style={{ width:'100%', marginTop:8, justifyContent:'center' }}
            disabled={loading}
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p style={{ textAlign:'center', color:'#a0a0a0',
                    fontSize:14, marginTop:24 }}>
          Already have an account?{' '}
          <Link to="/login"
            style={{ color:'#e50914', fontWeight:600 }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}