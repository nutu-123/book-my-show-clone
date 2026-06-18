import { useEffect, useState } from 'react'
import axiosInstance from '../utils/axiosInstance'
import Loader from '../components/common/Loader'
import {
  FiFilm, FiMap, FiBookOpen,
  FiCreditCard, FiPlus, FiUsers
} from 'react-icons/fi'

export default function AdminDashboard() {
  const [stats,   setStats]   = useState(null)
  const [loading, setLoading] = useState(true)
  const [tab,     setTab]     = useState('overview')

  // Movie form state
  const [movieForm, setMovieForm] = useState({
    title:'', description:'', duration:'', director:'',
    genres:'', languages:'', releaseDate:'',
    certificate:'UA', formats:'2D', posterUrl:''
  })
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    // Fetch stats
    Promise.all([
      axiosInstance.get('/movies?page=0&size=1'),
      axiosInstance.get('/theatres?page=0&size=1'),
      axiosInstance.get('/bookings/admin/all?page=0&size=1'),
      axiosInstance.get('/payments/admin/all?page=0&size=1')
    ])
      .then(([m, t, b, p]) => {
        setStats({
          movies:   m.data.totalElements  || 0,
          theatres: t.data.totalElements  || 0,
          bookings: b.data.totalElements  || 0,
          payments: p.data.totalElements  || 0
        })
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  const handleAddMovie = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await axiosInstance.post('/movies', {
        ...movieForm,
        duration:  parseInt(movieForm.duration),
        genres:    movieForm.genres.split(',').map(g => g.trim()),
        languages: movieForm.languages.split(',').map(l => l.trim()),
        formats:   movieForm.formats.split(',').map(f => f.trim()),
        releaseDate: movieForm.releaseDate
      })
      alert('Movie added successfully!')
      setMovieForm({
        title:'', description:'', duration:'', director:'',
        genres:'', languages:'', releaseDate:'',
        certificate:'UA', formats:'2D', posterUrl:''
      })
    } catch (e) {
      alert('Error: ' + (e.response?.data?.message || 'Failed'))
    } finally {
      setSubmitting(false)
    }
  }

  const TABS = [
    { id:'overview', label:'Overview',   icon:<FiFilm /> },
    { id:'movies',   label:'Add Movie',  icon:<FiPlus /> },
    { id:'theatres', label:'Theatres',   icon:<FiMap /> }
  ]

  return (
    <div className="container" style={{ padding:'32px 20px' }}>
      <h1 style={{ fontSize:24, fontWeight:700, marginBottom:28 }}>
        ⚙️ Admin Dashboard
      </h1>

      {/* Tab nav */}
      <div style={styles.tabs}>
        {TABS.map(t => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            style={{
              ...styles.tab,
              ...(tab === t.id ? styles.tabActive : {})
            }}
          >
            {t.icon} {t.label}
          </button>
        ))}
      </div>

      {/* Overview */}
      {tab === 'overview' && (
        <div>
          {loading ? (
            <Loader />
          ) : (
            <div style={styles.statsGrid}>
              {[
                { icon:<FiFilm size={24} />,    label:'Total Movies',   val:stats?.movies,   color:'#e50914' },
                { icon:<FiMap size={24} />,     label:'Theatres',       val:stats?.theatres, color:'#2196F3' },
                { icon:<FiBookOpen size={24} />,label:'Total Bookings', val:stats?.bookings, color:'#4caf50' },
                { icon:<FiCreditCard size={24}/>,label:'Transactions',  val:stats?.payments, color:'#f5c518' }
              ].map(s => (
                <div key={s.label} style={styles.statCard}>
                  <div style={{ color:s.color, marginBottom:12 }}>
                    {s.icon}
                  </div>
                  <h2 style={{
                    fontSize:36, fontWeight:800, color:s.color
                  }}>
                    {s.val?.toLocaleString() || 0}
                  </h2>
                  <p style={{ color:'#a0a0a0', fontSize:14, marginTop:4 }}>
                    {s.label}
                  </p>
                </div>
              ))}
            </div>
          )}

          {/* Quick links */}
          <div style={{ marginTop:32 }}>
            <h2 className="section-title">Quick Actions</h2>
            <div style={styles.actionsGrid}>
              {[
                { label:'Add New Movie',    action: () => setTab('movies') },
                { label:'Add Theatre',      action: () => setTab('theatres') },
                { label:'View All Bookings', action: () =>
                    window.open('/api/bookings/admin/all', '_blank') }
              ].map(a => (
                <button
                  key={a.label}
                  className="btn btn-secondary"
                  style={{ justifyContent:'center', padding:'16px' }}
                  onClick={a.action}
                >
                  {a.label}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Add Movie */}
      {tab === 'movies' && (
        <div style={{ maxWidth:640 }}>
          <h2 style={{ fontSize:18, fontWeight:700, marginBottom:20 }}>
            Add New Movie
          </h2>
          <div style={styles.formCard}>
            <form onSubmit={handleAddMovie}>
              {[
                ['title',       'Movie Title', 'text'],
                ['director',    'Director',    'text'],
                ['duration',    'Duration (minutes)', 'number'],
                ['releaseDate', 'Release Date', 'date'],
                ['genres',      'Genres (comma-separated)', 'text'],
                ['languages',   'Languages (comma-separated)', 'text'],
                ['formats',     'Formats (2D,3D,IMAX)', 'text'],
                ['certificate', 'Certificate', 'text'],
                ['posterUrl',   'Poster URL', 'url'],
              ].map(([name, label, type]) => (
                <div className="form-group" key={name}>
                  <label>{label}</label>
                  <input
                    type={type}
                    value={movieForm[name]}
                    onChange={(e) =>
                      setMovieForm({ ...movieForm, [name]:e.target.value })}
                    required={['title','director','duration',
                                'releaseDate','genres','languages']
                              .includes(name)}
                    style={styles.input}
                  />
                </div>
              ))}

              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={movieForm.description}
                  onChange={(e) =>
                    setMovieForm({ ...movieForm, description:e.target.value })}
                  rows={4} required
                  style={{
                    ...styles.input, resize:'vertical', minHeight:100
                  }}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-lg"
                style={{ width:'100%', justifyContent:'center' }}
                disabled={submitting}
              >
                {submitting ? 'Adding...' : '+ Add Movie'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Theatres tab */}
      {tab === 'theatres' && (
        <div style={styles.formCard}>
          <p style={{ color:'#a0a0a0', marginBottom:12 }}>
            Use the API directly to add theatres and screens.
          </p>
          <pre style={{
            background:'#0d0d1a', borderRadius:8, padding:16,
            fontSize:12, color:'#4caf50', overflowX:'auto'
          }}>
{`POST /api/theatres
{
  "name": "PVR Cinemas",
  "city": "Mumbai",
  "state": "Maharashtra",
  "address": "Phoenix Mall, Kurla"
}

POST /api/theatres/{id}/screens
{
  "screenName": "Screen 1",
  "screenNumber": 1,
  "supportedFormats": ["2D","3D"],
  "seatLayout": { ... }
}`}
          </pre>
        </div>
      )}
    </div>
  )
}

const styles = {
  tabs: {
    display:'flex', gap:8, marginBottom:28,
    borderBottom:'1px solid #2a2a4a', paddingBottom:0
  },
  tab: {
    display:'flex', alignItems:'center', gap:6,
    padding:'10px 18px', background:'transparent',
    border:'none', color:'#a0a0a0', cursor:'pointer',
    fontSize:14, fontWeight:500, borderRadius:'8px 8px 0 0',
    transition:'all 0.2s', borderBottom:'2px solid transparent',
    marginBottom:-1
  },
  tabActive: {
    color:'#e50914', borderBottomColor:'#e50914',
    background:'rgba(229,9,20,0.05)'
  },
  statsGrid: {
    display:'grid',
    gridTemplateColumns:'repeat(auto-fit, minmax(200px, 1fr))',
    gap:20
  },
  statCard: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:'24px 20px', textAlign:'center'
  },
  actionsGrid: {
    display:'grid',
    gridTemplateColumns:'repeat(auto-fit, minmax(180px, 1fr))',
    gap:16
  },
  formCard: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:'28px 24px'
  },
  input: {
    width:'100%', background:'#16213e',
    border:'1px solid #2a2a4a', borderRadius:10,
    padding:'11px 14px', color:'#fff', fontSize:14
  }
}