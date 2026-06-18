import { useNavigate } from 'react-router-dom'
import { FiStar, FiClock } from 'react-icons/fi'

export default function MovieCard({ movie }) {
  const navigate = useNavigate()

  return (
    <div style={styles.card}
      onClick={() => navigate(`/movies/${movie.id}`)}>
      {/* Poster */}
      <div style={styles.posterWrap}>
        {movie.posterUrl ? (
          <img
            src={movie.posterUrl}
            alt={movie.title}
            style={styles.poster}
            onError={(e) => {
              e.target.src = `https://placehold.co/300x450/1a1a2e/e50914?text=${encodeURIComponent(movie.title)}`
            }}
          />
        ) : (
          <div style={styles.noImage}>
            <span style={{ fontSize:40 }}>🎬</span>
          </div>
        )}
        {/* Rating badge */}
        <div style={styles.ratingBadge}>
          <FiStar size={10} color="#f5c518" fill="#f5c518" />
          <span>{movie.rating?.toFixed(1) || 'N/A'}</span>
        </div>
        {/* Certificate */}
        {movie.certificate && (
          <div style={styles.certBadge}>{movie.certificate}</div>
        )}
      </div>

      {/* Info */}
      <div style={styles.info}>
        <h3 style={styles.title}>{movie.title}</h3>
        <div style={styles.meta}>
          <span style={styles.metaItem}>
            {movie.genres?.slice(0, 2).join(' • ')}
          </span>
          <span style={styles.metaItem}>
            <FiClock size={11} />
            {movie.durationFormatted || `${movie.duration}m`}
          </span>
        </div>
        <div style={styles.langs}>
          {movie.languages?.slice(0, 3).map(l => (
            <span key={l} style={styles.lang}>{l}</span>
          ))}
        </div>
        <button
          className="btn btn-primary"
          style={{ width:'100%', marginTop:10, fontSize:13 }}
          onClick={(e) => {
            e.stopPropagation()
            navigate(`/movies/${movie.id}`)
          }}
        >
          Book Tickets
        </button>
      </div>
    </div>
  )
}

const styles = {
  card: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, overflow:'hidden', cursor:'pointer',
    transition:'all 0.3s ease',
    ':hover': { transform:'translateY(-4px)' }
  },
  posterWrap: { position:'relative', paddingBottom:'150%', overflow:'hidden' },
  poster: {
    position:'absolute', top:0, left:0,
    width:'100%', height:'100%', objectFit:'cover'
  },
  noImage: {
    position:'absolute', top:0, left:0,
    width:'100%', height:'100%',
    background:'#16213e',
    display:'flex', alignItems:'center', justifyContent:'center'
  },
  ratingBadge: {
    position:'absolute', top:8, right:8,
    background:'rgba(0,0,0,0.8)',
    color:'#f5c518', borderRadius:6, padding:'3px 8px',
    fontSize:11, fontWeight:700, display:'flex', gap:4, alignItems:'center'
  },
  certBadge: {
    position:'absolute', top:8, left:8,
    background:'rgba(229,9,20,0.9)',
    color:'white', borderRadius:4, padding:'2px 7px',
    fontSize:10, fontWeight:700
  },
  info: { padding:'14px 14px 16px' },
  title: {
    fontSize:15, fontWeight:700, color:'#fff',
    marginBottom:6, lineHeight:1.3,
    overflow:'hidden', textOverflow:'ellipsis',
    display:'-webkit-box',
    WebkitLineClamp:2, WebkitBoxOrient:'vertical'
  },
  meta: { display:'flex', flexDirection:'column', gap:4, marginBottom:8 },
  metaItem: {
    color:'#a0a0a0', fontSize:11,
    display:'flex', alignItems:'center', gap:4
  },
  langs: { display:'flex', flexWrap:'wrap', gap:4 },
  lang: {
    background:'#16213e', color:'#a0a0a0',
    border:'1px solid #2a2a4a', borderRadius:4,
    padding:'2px 8px', fontSize:10
  }
}