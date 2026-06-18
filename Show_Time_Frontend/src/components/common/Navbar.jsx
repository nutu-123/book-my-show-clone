import { Link, useNavigate } from 'react-router-dom'
import { useSelector, useDispatch } from 'react-redux'
import { useState } from 'react'
import { logout } from '../../store/slices/authSlice'
import {
  FiFilm, FiUser, FiLogOut, FiMenu, FiX,
  FiBookOpen, FiSettings
} from 'react-icons/fi'

export default function Navbar() {
  const { isAuthenticated, user } = useSelector((s) => s.auth)
  const dispatch  = useDispatch()
  const navigate  = useNavigate()
  const [open, setOpen] = useState(false)
  const isAdmin = user?.roles?.includes('ROLE_ADMIN')

  const handleLogout = () => {
    dispatch(logout())
    navigate('/')
  }

  return (
    <nav style={styles.nav}>
      <div className="container" style={styles.inner}>
        {/* Logo */}
        <Link to="/" style={styles.logo}>
          <FiFilm size={24} color="#e50914" />
          <span>Show<span style={{ color:'#e50914' }}>Time</span></span>
        </Link>

        {/* Desktop links */}
        <div style={styles.links}>
          <Link to="/movies" style={styles.link}>Movies</Link>
          {isAuthenticated && (
            <Link to="/my-bookings" style={styles.link}>My Bookings</Link>
          )}
          {isAdmin && (
            <Link to="/admin" style={styles.link}>Admin</Link>
          )}
        </div>

        {/* Auth buttons */}
        <div style={styles.auth}>
          {isAuthenticated ? (
            <div style={styles.userMenu}>
              <span style={styles.userName}>
                <FiUser size={14} /> {user?.name || 'User'}
              </span>
              <div style={styles.dropdown}>
                <button
                  onClick={() => navigate('/profile')}
                  style={styles.dropdownItem}
                >
                  <FiUser size={14} /> Profile
                </button>
                <button
                  onClick={() => navigate('/my-bookings')}
                  style={styles.dropdownItem}
                >
                  <FiBookOpen size={14} /> My Bookings
                </button>
                {isAdmin && (
                  <button
                    onClick={() => navigate('/admin')}
                    style={styles.dropdownItem}
                  >
                    <FiSettings size={14} /> Admin Panel
                  </button>
                )}
                <button
                  onClick={handleLogout}
                  style={{ ...styles.dropdownItem, color:'#e50914' }}
                >
                  <FiLogOut size={14} /> Logout
                </button>
              </div>
            </div>
          ) : (
            <div style={{ display:'flex', gap:'12px' }}>
              <Link to="/login"
                className="btn btn-secondary btn-sm">
                Login
              </Link>
              <Link to="/register"
                className="btn btn-primary btn-sm">
                Sign Up
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  )
}

const styles = {
  nav: {
    background: '#0d0d1a',
    borderBottom: '1px solid #2a2a4a',
    position: 'sticky', top: 0, zIndex: 100,
    backdropFilter: 'blur(10px)'
  },
  inner: {
    display: 'flex', alignItems: 'center',
    justifyContent: 'space-between',
    height: 64, gap: 20
  },
  logo: {
    display: 'flex', alignItems: 'center', gap: 8,
    fontSize: 22, fontWeight: 700, color: '#fff'
  },
  links: { display:'flex', gap:24 },
  link: {
    color:'#a0a0a0', fontSize:14, fontWeight:500,
    transition:'color 0.2s'
  },
  auth: { display:'flex', alignItems:'center', gap:12 },
  userMenu: {
    position:'relative',
    '&:hover > div': { display:'block' }
  },
  userName: {
    display:'flex', alignItems:'center', gap:6,
    color:'#fff', fontSize:14, fontWeight:500,
    cursor:'pointer', padding:'6px 12px',
    borderRadius:8, background:'#1a1a2e',
    border:'1px solid #2a2a4a'
  },
  dropdown: {
    position:'absolute', top:'110%', right:0,
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:10, padding:'8px', minWidth:180,
    boxShadow:'0 8px 32px rgba(0,0,0,0.4)',
    display:'none', zIndex:200
  },
  dropdownItem: {
    display:'flex', alignItems:'center', gap:8,
    width:'100%', padding:'10px 14px',
    background:'transparent', color:'#a0a0a0',
    fontSize:13, borderRadius:6,
    cursor:'pointer', textAlign:'left',
    transition:'all 0.2s'
  }
}