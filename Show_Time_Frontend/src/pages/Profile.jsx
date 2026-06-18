import { useState, useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { fetchProfile, logout } from '../store/slices/authSlice'
import { useNavigate } from 'react-router-dom'
import axiosInstance from '../utils/axiosInstance'
import toast from 'react-hot-toast'
import { FiUser, FiMail, FiPhone, FiLogOut, FiSave } from 'react-icons/fi'

export default function Profile() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { user } = useSelector((s) => s.auth)
  const [form, setForm]     = useState({ name:'', phone:'' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    dispatch(fetchProfile())
  }, [dispatch])

  useEffect(() => {
    if (user) {
      setForm({ name: user.name || '', phone: user.phone || '' })
    }
  }, [user])

  const handleUpdate = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      await axiosInstance.put(
        `/auth/me?name=${form.name}&phone=${form.phone}`
      )
      dispatch(fetchProfile())
      toast.success('Profile updated successfully')
    } catch {
      toast.error('Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  const handleLogout = () => {
    dispatch(logout())
    navigate('/')
  }

  return (
    <div style={{ maxWidth:580, margin:'0 auto', padding:'40px 20px' }}>
      <h1 style={{ fontSize:24, fontWeight:700, marginBottom:28 }}>
        👤 My Profile
      </h1>

      <div style={styles.card}>
        {/* Avatar */}
        <div style={styles.avatar}>
          <span style={{ fontSize:40 }}>
            {user?.name?.charAt(0)?.toUpperCase() || '?'}
          </span>
        </div>

        <h2 style={{ fontSize:20, fontWeight:700, marginTop:12 }}>
          {user?.name}
        </h2>

        {/* Roles */}
        <div style={{ display:'flex', gap:8, marginTop:8 }}>
          {user?.roles?.map(r => (
            <span key={r} style={styles.roleBadge}>
              {r.replace('ROLE_', '')}
            </span>
          ))}
        </div>

        {/* Info */}
        <div style={styles.infoBox}>
          <div style={styles.infoRow}>
            <FiMail size={15} color="#a0a0a0" />
            <span style={{ color:'#a0a0a0', fontSize:14 }}>
              {user?.email}
            </span>
          </div>
          {user?.phone && (
            <div style={styles.infoRow}>
              <FiPhone size={15} color="#a0a0a0" />
              <span style={{ color:'#a0a0a0', fontSize:14 }}>
                {user.phone}
              </span>
            </div>
          )}
        </div>

        {/* Edit form */}
        <form onSubmit={handleUpdate} style={{ marginTop:24 }}>
          <div className="form-group">
            <label>Display Name</label>
            <div style={{ position:'relative' }}>
              <FiUser size={15} color="#a0a0a0"
                style={{ position:'absolute', left:12,
                         top:'50%', transform:'translateY(-50%)' }} />
              <input
                value={form.name}
                onChange={(e) =>
                  setForm({ ...form, name:e.target.value })}
                style={styles.input}
              />
            </div>
          </div>
          <div className="form-group">
            <label>Phone Number</label>
            <div style={{ position:'relative' }}>
              <FiPhone size={15} color="#a0a0a0"
                style={{ position:'absolute', left:12,
                         top:'50%', transform:'translateY(-50%)' }} />
              <input
                value={form.phone}
                onChange={(e) =>
                  setForm({ ...form, phone:e.target.value })}
                placeholder="9876543210"
                style={styles.input}
              />
            </div>
          </div>
          <button
            type="submit"
            className="btn btn-primary"
            style={{ width:'100%', justifyContent:'center' }}
            disabled={saving}
          >
            <FiSave size={15} />
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
        </form>

        <button
          className="btn"
          style={{
            width:'100%', marginTop:12, justifyContent:'center',
            background:'rgba(244,67,54,0.08)',
            border:'1px solid rgba(244,67,54,0.3)',
            color:'#f44336'
          }}
          onClick={handleLogout}
        >
          <FiLogOut size={15} /> Sign Out
        </button>
      </div>
    </div>
  )
}

const styles = {
  card: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:16, padding:'32px 28px', textAlign:'center'
  },
  avatar: {
    width:80, height:80, borderRadius:'50%',
    background:'linear-gradient(135deg,#e50914,#b20710)',
    display:'flex', alignItems:'center', justifyContent:'center',
    margin:'0 auto', color:'white', fontWeight:800
  },
  roleBadge: {
    background:'rgba(229,9,20,0.12)',
    border:'1px solid rgba(229,9,20,0.3)',
    color:'#e50914', borderRadius:20,
    padding:'3px 12px', fontSize:11, fontWeight:700
  },
  infoBox: {
    background:'#16213e', borderRadius:10, padding:'12px 16px',
    marginTop:16, display:'flex', flexDirection:'column', gap:8
  },
  infoRow: {
    display:'flex', alignItems:'center', gap:10,
    justifyContent:'center'
  },
  input: {
    width:'100%', background:'#16213e',
    border:'1px solid #2a2a4a', borderRadius:10,
    padding:'12px 12px 12px 40px', color:'#fff', fontSize:14
  }
}