import { useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchShows } from '../store/slices/showSlice'
import Loader from '../components/common/Loader'
import { FiClock, FiMonitor, FiMapPin } from 'react-icons/fi'

export default function TheatreSelection() {
  const [params]   = useSearchParams()
  const dispatch   = useDispatch()
  const navigate   = useNavigate()
  const { list, loading } = useSelector((s) => s.shows)

  const movieId = params.get('movieId')
  const city    = params.get('city')    || 'Mumbai'
  const date    = params.get('date')    || new Date().toISOString().split('T')[0]

  useEffect(() => {
    if (movieId) dispatch(fetchShows({ movieId, city, date }))
  }, [movieId, city, date, dispatch])

  // Group shows by theatre
  const grouped = list.reduce((acc, show) => {
    const key = show.theatreId
    if (!acc[key]) acc[key] = { name:show.theatreName, shows:[] }
    acc[key].shows.push(show)
    return acc
  }, {})

  return (
    <div className="container" style={{ padding:'32px 20px' }}>
      <div style={{ marginBottom:24 }}>
        <h1 style={{ fontSize:22, fontWeight:700 }}>Select Show</h1>
        <p style={{ color:'#a0a0a0', fontSize:14, marginTop:4 }}>
          <FiMapPin size={12} /> {city} • {date}
        </p>
      </div>

      {loading ? (
        <Loader />
      ) : Object.keys(grouped).length === 0 ? (
        <div className="text-center" style={{ padding:60 }}>
          <p style={{ fontSize:48 }}>🎭</p>
          <p style={{ color:'#a0a0a0', marginTop:16, fontSize:16 }}>
            No shows available in {city} on {date}
          </p>
          <p style={{ color:'#666', fontSize:13, marginTop:8 }}>
            Try selecting a different city or date
          </p>
        </div>
      ) : (
        <div style={{ display:'flex', flexDirection:'column', gap:20 }}>
          {Object.values(grouped).map((theatre, i) => (
            <div key={i} style={styles.theatreCard}>
              <div style={styles.theatreHeader}>
                <div>
                  <h3 style={{ fontSize:16, fontWeight:700 }}>
                    {theatre.name}
                  </h3>
                </div>
              </div>

              <div style={styles.showList}>
                {theatre.shows
                  .sort((a, b) =>
                    a.startTimeFormatted?.localeCompare(
                      b.startTimeFormatted
                    ))
                  .map((show) => (
                    <button
                      key={show.id}
                      onClick={() => navigate(`/seats/${show.id}`)}
                      disabled={show.status === 'HOUSEFULL'}
                      style={{
                        ...styles.showBtn,
                        ...(show.status === 'HOUSEFULL'
                          ? styles.showBtnFull : {})
                      }}
                    >
                      <div style={{ fontWeight:700, fontSize:15 }}>
                        {show.startTimeFormatted}
                      </div>
                      <div style={styles.showMeta}>
                        <span>
                          <FiMonitor size={11} /> {show.screenName}
                        </span>
                        <span>{show.format}</span>
                        <span>{show.language}</span>
                      </div>
                      <div style={styles.availability}>
                        {show.status === 'HOUSEFULL' ? (
                          <span style={styles.housefull}>Housefull</span>
                        ) : (
                          <span style={styles.available}>
                            {show.availableSeats} seats
                          </span>
                        )}
                      </div>

                      {/* Price range */}
                      {show.seatPrices?.length > 0 && (
                        <div style={styles.priceRange}>
                          ₹{Math.min(...show.seatPrices.map(p => p.price))}
                          {' - '}
                          ₹{Math.max(...show.seatPrices.map(p => p.price))}
                        </div>
                      )}
                    </button>
                  ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

const styles = {
  theatreCard: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, overflow:'hidden'
  },
  theatreHeader: {
    padding:'16px 20px', borderBottom:'1px solid #2a2a4a',
    display:'flex', justifyContent:'space-between', alignItems:'center'
  },
  showList: {
    padding:'16px 20px',
    display:'flex', flexWrap:'wrap', gap:12
  },
  showBtn: {
    background:'#16213e', border:'2px solid #2a2a4a',
    borderRadius:10, padding:'12px 16px',
    cursor:'pointer', textAlign:'left', minWidth:140,
    transition:'all 0.2s', color:'#fff'
  },
  showBtnFull: {
    opacity:0.5, cursor:'not-allowed',
    borderColor:'#333'
  },
  showMeta: {
    display:'flex', gap:8, marginTop:4,
    color:'#a0a0a0', fontSize:11, flexWrap:'wrap'
  },
  availability: { marginTop:6 },
  housefull: {
    color:'#e50914', fontSize:11, fontWeight:700
  },
  available: {
    color:'#4caf50', fontSize:11, fontWeight:600
  },
  priceRange: {
    color:'#f5c518', fontSize:12, fontWeight:600, marginTop:4
  }
}