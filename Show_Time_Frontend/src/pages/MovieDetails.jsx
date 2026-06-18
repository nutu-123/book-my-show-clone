import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  fetchMovieById, clearCurrentMovie
} from '../store/slices/movieSlice'
import { fetchCities, setSelectedCity } from '../store/slices/showSlice'
import Loader from '../components/common/Loader'
import {
  FiStar, FiClock, FiCalendar,
  FiGlobe, FiPlay, FiMapPin
} from 'react-icons/fi'
import { format } from 'date-fns'

export default function MovieDetails() {
  const { id } = useParams()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { current: movie, loading } = useSelector((s) => s.movies)
  const { cities, selectedCity } = useSelector((s) => s.shows)

  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  )

  useEffect(() => {
    dispatch(fetchMovieById(id))
    dispatch(fetchCities())
    return () => dispatch(clearCurrentMovie())
  }, [id, dispatch])

  const handleBookNow = () => {
    navigate(
      `/shows?movieId=${movie.id}&city=${selectedCity}&date=${selectedDate}`
    )
  }

  // Generate next 7 dates
  const dates = Array.from({ length: 7 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() + i)
    return d
  })

  if (loading) return <Loader />
  if (!movie) return (
    <div className="text-center" style={{ padding: 80 }}>
      Movie not found
    </div>
  )

  return (
    <div>
      {/* Hero banner */}
      <div style={styles.hero}>
        <div style={styles.heroBg} />
        <div className="container" style={styles.heroContent}>
          <div style={styles.posterWrap}>
            <img
              src={movie.posterUrl ||
                `https://placehold.co/280x420/1a1a2e/e50914?text=${encodeURIComponent(movie.title)}`}
              alt={movie.title}
              style={styles.poster}
              onError={(e) => {
                e.target.src = `https://placehold.co/280x420/1a1a2e/e50914?text=${encodeURIComponent(movie.title)}`
              }}
            />
          </div>

          <div style={styles.details}>
            <h1 style={styles.title}>{movie.title}</h1>

            <div style={styles.metaRow}>
              <span style={styles.rating}>
                <FiStar size={16} color="#f5c518" fill="#f5c518" />
                {movie.rating?.toFixed(1)} / 5
                <span style={{ color: '#666', fontSize: 13 }}>
                  ({movie.totalRatings?.toLocaleString()} ratings)
                </span>
              </span>
              {movie.certificate && (
                <span style={styles.cert}>{movie.certificate}</span>
              )}
            </div>

            <div style={styles.infoGrid}>
              <div style={styles.infoItem}>
                <FiClock size={14} color="#a0a0a0" />
                <span>{movie.durationFormatted}</span>
              </div>
              <div style={styles.infoItem}>
                <FiCalendar size={14} color="#a0a0a0" />
                <span>
                  {movie.releaseDate
                    ? format(new Date(movie.releaseDate), 'dd MMM yyyy')
                    : 'N/A'}
                </span>
              </div>
              <div style={styles.infoItem}>
                <FiGlobe size={14} color="#a0a0a0" />
                <span>{movie.languages?.join(', ')}</span>
              </div>
            </div>

            <div style={styles.genres}>
              {movie.genres?.map(g => (
                <span key={g} style={styles.genreBadge}>{g}</span>
              ))}
            </div>

            <p style={styles.desc}>{movie.description}</p>

            {/* Booking section */}
            <div style={styles.bookingBox}>
              <div style={styles.bookingRow}>
                <div className="form-group" style={{ marginBottom: 0, flex: 1 }}>
                  <label style={{ color: '#a0a0a0', fontSize: 12 }}>
                    <FiMapPin size={11} /> City
                  </label>
                  <select
                    value={selectedCity || ''}
                    onChange={(e) => dispatch(setSelectedCity(e.target.value))}
                    style={styles.bookSelect}
                  >
                    {cities.map(c => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Date selector */}
              <div style={styles.dates}>
                {dates.map(d => {
                  const val = d.toISOString().split('T')[0]
                  const active = val === selectedDate
                  return (
                    <button
                      key={val}
                      onClick={() => setSelectedDate(val)}
                      style={{
                        ...styles.dateBtn,
                        ...(active ? styles.dateBtnActive : {})
                      }}
                    >
                      <span style={{ fontSize: 11 }}>
                        {format(d, 'EEE')}
                      </span>
                      <span style={{ fontSize: 16, fontWeight: 700 }}>
                        {format(d, 'd')}
                      </span>
                      <span style={{ fontSize: 10 }}>
                        {format(d, 'MMM')}
                      </span>
                    </button>
                  )
                })}
              </div>

              <button
                className="btn btn-primary btn-lg"
                onClick={handleBookNow}
                style={{ width: '100%', justifyContent: 'center' }}
              >
                🎬 Book Tickets
              </button>

              {movie.trailerUrl && (
                <a
                  href={movie.trailerUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="btn btn-secondary"
                  style={{ width: '100%', justifyContent: 'center', marginTop: 10, display: 'flex', alignItems: 'center', gap: 8 }}
                >
                  <FiPlay size={16} /> Watch Trailer
                </a>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Cast */}
      {movie.cast?.length > 0 && (
        <div className="container" style={{ padding: '40px 20px' }}>
          <h2 className="section-title">Cast</h2>
          <div style={styles.castGrid}>
            {movie.cast.slice(0, 8).map((c, i) => (
              <div key={i} style={styles.castCard}>
                <div style={styles.castAvatar}>
                  {c.photoUrl
                    ? <img src={c.photoUrl} alt={c.name}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    : <span style={{ fontSize: 28 }}>🎭</span>
                  }
                </div>
                <p style={{ fontWeight: 600, fontSize: 13 }}>{c.name}</p>
                <p style={{ color: '#a0a0a0', fontSize: 11 }}>{c.character}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

const styles = {
  hero: {
    position: 'relative', padding: '60px 0',
    background: 'linear-gradient(to bottom, #0d0d1a, #1a1a2e)'
  },
  heroBg: { position: 'absolute', inset: 0, zIndex: 0 },
  heroContent: {
    position: 'relative', zIndex: 1,
    display: 'flex', gap: 40, alignItems: 'flex-start',
    maxWidth: 1200, margin: '0 auto', padding: '0 20px'
  },
  posterWrap: { flexShrink: 0 },
  poster: { width: 220, borderRadius: 12, boxShadow: '0 20px 60px rgba(0,0,0,0.5)' },
  details: { flex: 1 },
  title: { fontSize: 34, fontWeight: 800, marginBottom: 12, color: '#fff' },
  metaRow: {
    display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16
  },
  rating: {
    display: 'flex', alignItems: 'center', gap: 6,
    fontSize: 16, fontWeight: 700, color: '#f5c518'
  },
  cert: {
    background: 'rgba(229,9,20,0.2)', color: '#e50914',
    border: '1px solid rgba(229,9,20,0.3)',
    borderRadius: 4, padding: '2px 10px', fontSize: 12, fontWeight: 700
  },
  infoGrid: { display: 'flex', gap: 24, marginBottom: 16, flexWrap: 'wrap' },
  infoItem: {
    display: 'flex', alignItems: 'center', gap: 6,
    color: '#a0a0a0', fontSize: 13
  },
  genres: { display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 },
  genreBadge: {
    background: '#16213e', border: '1px solid #2a2a4a',
    borderRadius: 20, padding: '4px 14px', fontSize: 12, color: '#fff'
  },
  desc: { color: '#c0c0c0', fontSize: 14, lineHeight: 1.8, marginBottom: 24 },
  bookingBox: {
    background: '#16213e', border: '1px solid #2a2a4a',
    borderRadius: 12, padding: 20
  },
  bookingRow: { display: 'flex', gap: 12, marginBottom: 16 },
  bookSelect: {
    background: '#1a1a2e', border: '1px solid #2a2a4a',
    borderRadius: 8, padding: '8px 12px', color: '#fff', fontSize: 13, width: '100%'
  },
  dates: { display: 'flex', gap: 8, marginBottom: 16, overflowX: 'auto' },
  dateBtn: {
    flexShrink: 0, display: 'flex', flexDirection: 'column',
    alignItems: 'center', gap: 2, padding: '8px 14px',
    background: '#1a1a2e', border: '1px solid #2a2a4a',
    borderRadius: 8, color: '#a0a0a0', cursor: 'pointer',
    minWidth: 52, transition: 'all 0.2s'
  },
  dateBtnActive: {
    background: 'rgba(229,9,20,0.15)', border: '1px solid #e50914', color: '#fff'
  },
  castGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))',
    gap: 16
  },
  castCard: { textAlign: 'center' },
  castAvatar: {
    width: 72, height: 72, borderRadius: '50%',
    background: '#1a1a2e', border: '2px solid #2a2a4a',
    margin: '0 auto 8px',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    overflow: 'hidden'
  }
}