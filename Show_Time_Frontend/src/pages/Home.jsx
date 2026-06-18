import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { fetchTrending, fetchMovies, fetchGenres } from '../store/slices/movieSlice'
import { fetchCities, setSelectedCity } from '../store/slices/showSlice'
import MovieCard from '../components/ui/MovieCard'
import Loader from '../components/common/Loader'
import { FiSearch, FiMapPin } from 'react-icons/fi'

export default function Home() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { trending, list, loading } = useSelector((s) => s.movies)
  const { cities, selectedCity }    = useSelector((s) => s.shows)
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    dispatch(fetchTrending())
    dispatch(fetchMovies({ page: 0, size: 8 }))
    dispatch(fetchCities())
  }, [dispatch])

  const handleSearch = (e) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      navigate(`/movies?q=${searchQuery}`)
    }
  }

  return (
    <div>
      {/* Hero Banner */}
      <section style={styles.hero}>
        <div style={styles.heroBg} />
        <div className="container" style={styles.heroContent}>
          <h1 style={styles.heroTitle}>
            Book Movie Tickets<br />
            <span style={{ color:'#e50914' }}>Instantly</span>
          </h1>
          <p style={styles.heroSub}>
            Find the latest movies playing near you
          </p>

          {/* City + Search bar */}
          <div style={styles.searchBar}>
            <div style={styles.cityPicker}>
              <FiMapPin size={16} color="#e50914" />
              <select
                value={selectedCity}
                onChange={(e) => dispatch(setSelectedCity(e.target.value))}
                style={styles.citySelect}
              >
                {cities.length > 0
                  ? cities.map(c => <option key={c} value={c}>{c}</option>)
                  : <option>Mumbai</option>
                }
              </select>
            </div>
            <div style={styles.searchDivider} />
            <form onSubmit={handleSearch} style={styles.searchForm}>
              <FiSearch size={18} color="#a0a0a0" />
              <input
                placeholder="Search movies, theatres..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                style={styles.searchInput}
              />
              <button
                type="submit"
                className="btn btn-primary btn-sm"
              >
                Search
              </button>
            </form>
          </div>
        </div>
      </section>

      {/* Trending Movies */}
      <section style={{ padding:'48px 0' }}>
        <div className="container">
          <h2 className="section-title">🔥 Trending Now</h2>
          {loading ? (
            <Loader />
          ) : (
            <div style={styles.movieGrid}>
              {(trending.length > 0 ? trending : list)
                .slice(0, 8)
                .map(movie => (
                  <MovieCard key={movie.id} movie={movie} />
                ))
              }
            </div>
          )}
          {list.length > 8 && (
            <div className="text-center mt-32">
              <button
                className="btn btn-secondary"
                onClick={() => navigate('/movies')}
              >
                View All Movies →
              </button>
            </div>
          )}
        </div>
      </section>

      {/* Features Section */}
      <section style={{ padding:'40px 0', background:'#0d0d1a' }}>
        <div className="container">
          <div style={styles.features}>
            {[
              { icon:'🎬', title:'10,000+ Movies', desc:'Largest collection' },
              { icon:'🏟️', title:'500+ Theatres', desc:'Across India' },
              { icon:'💺', title:'Easy Seat Selection', desc:'Pick your favourite seat' },
              { icon:'⚡', title:'Instant Confirmation', desc:'Get tickets in seconds' }
            ].map(f => (
              <div key={f.title} style={styles.featureCard}>
                <span style={{ fontSize:32 }}>{f.icon}</span>
                <h3 style={{ fontSize:15, fontWeight:700, marginTop:8 }}>{f.title}</h3>
                <p style={{ color:'#a0a0a0', fontSize:12, marginTop:4 }}>{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}

const styles = {
  hero: {
    position:'relative', padding:'80px 0 60px',
    overflow:'hidden', minHeight:420,
    display:'flex', alignItems:'center'
  },
  heroBg: {
    position:'absolute', inset:0,
    background:'linear-gradient(135deg, #0d0d1a 0%, #1a0a0a 100%)',
    zIndex:0
  },
  heroContent: { position:'relative', zIndex:1, textAlign:'center' },
  heroTitle: { fontSize:48, fontWeight:800, lineHeight:1.2, marginBottom:16 },
  heroSub: { color:'#a0a0a0', fontSize:18, marginBottom:36 },
  searchBar: {
    maxWidth:680, margin:'0 auto',
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:50, display:'flex',
    alignItems:'center', overflow:'hidden',
    padding:'6px 8px 6px 16px',
    boxShadow:'0 8px 32px rgba(229,9,20,0.15)'
  },
  cityPicker: {
    display:'flex', alignItems:'center', gap:6,
    paddingRight:12, flexShrink:0
  },
  citySelect: {
    background:'transparent', border:'none',
    color:'#fff', fontSize:13, fontWeight:500, cursor:'pointer'
  },
  searchDivider: {
    width:1, height:20, background:'#2a2a4a', margin:'0 12px'
  },
  searchForm: {
    flex:1, display:'flex', alignItems:'center', gap:10
  },
  searchInput: {
    flex:1, background:'transparent', border:'none',
    color:'#fff', fontSize:14
  },
  movieGrid: {
    display:'grid',
    gridTemplateColumns:'repeat(auto-fill, minmax(200px, 1fr))',
    gap:20
  },
  features: {
    display:'grid',
    gridTemplateColumns:'repeat(auto-fit, minmax(200px, 1fr))',
    gap:24
  },
  featureCard: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:'24px 20px', textAlign:'center'
  }
}