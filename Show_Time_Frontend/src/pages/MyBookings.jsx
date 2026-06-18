import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import {
  fetchUserBookings, cancelBooking
} from '../store/slices/bookingSlice'
import Loader from '../components/common/Loader'
import { FiEye, FiXCircle } from 'react-icons/fi'

const STATUS_COLOR = {
  CONFIRMED: '#4caf50',
  PENDING:   '#ff9800',
  CANCELLED: '#f44336',
  EXPIRED:   '#666'
}

export default function MyBookings() {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()
  const { userBookings, loading, totalPages } =
    useSelector((s) => s.booking)
  const [page, setPage] = useState(0)

  useEffect(() => {
    dispatch(fetchUserBookings({ page, size:10 }))
  }, [dispatch, page])

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this booking? A refund will be initiated.'))
      return
    dispatch(cancelBooking({ id, reason:'Cancelled by user' }))
  }

  return (
    <div className="container" style={{ padding:'32px 20px' }}>
      <h1 style={{ fontSize:24, fontWeight:700, marginBottom:24 }}>
        🎫 My Bookings
      </h1>

      {loading ? (
        <Loader />
      ) : userBookings.length === 0 ? (
        <div className="text-center" style={{ padding:60 }}>
          <p style={{ fontSize:48 }}>🎭</p>
          <p style={{ color:'#a0a0a0', marginTop:16, fontSize:16 }}>
            No bookings yet
          </p>
          <button
            className="btn btn-primary"
            style={{ marginTop:20 }}
            onClick={() => navigate('/movies')}
          >
            Book Your First Movie
          </button>
        </div>
      ) : (
        <>
          <div style={{ display:'flex', flexDirection:'column', gap:16 }}>
            {userBookings.map(b => (
              <div key={b.id} style={styles.card}>
                <div style={styles.cardLeft}>
                  <h3 style={{ fontSize:16, fontWeight:700 }}>
                    {b.movieTitle}
                  </h3>
                  <p style={styles.refText}>
                    Ref: {b.bookingReference}
                  </p>
                  <div style={styles.infoRow}>
                    <span style={styles.info}>{b.theatreName}</span>
                    <span style={styles.info}>•</span>
                    <span style={styles.info}>{b.showDate}</span>
                    <span style={styles.info}>•</span>
                    <span style={styles.info}>{b.showTime}</span>
                  </div>
                  <div style={styles.seats}>
                    {b.seatNumbers?.map(s => (
                      <span key={s} style={styles.seat}>{s}</span>
                    ))}
                  </div>
                </div>

                <div style={styles.cardRight}>
                  <span style={{
                    ...styles.statusBadge,
                    color: STATUS_COLOR[b.status] || '#fff',
                    borderColor: STATUS_COLOR[b.status] + '44' || '#2a2a4a',
                    background:  STATUS_COLOR[b.status] + '22' || '#1a1a2e'
                  }}>
                    {b.status}
                  </span>
                  <p style={{
                    fontSize:18, fontWeight:800,
                    color:'#f5c518', marginTop:8
                  }}>
                    ₹{b.grandTotal}
                  </p>
                  <div style={{ display:'flex', gap:8, marginTop:12 }}>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() =>
                        navigate(`/booking/confirmation/${b.id}`)
                      }
                    >
                      <FiEye size={13} /> View
                    </button>
                    {b.status === 'CONFIRMED' && (
                      <button
                        className="btn btn-sm"
                        style={{
                          background:'rgba(244,67,54,0.1)',
                          border:'1px solid rgba(244,67,54,0.3)',
                          color:'#f44336', borderRadius:8
                        }}
                        onClick={() => handleCancel(b.id)}
                      >
                        <FiXCircle size={13} /> Cancel
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div style={{
              display:'flex', justifyContent:'center',
              gap:16, marginTop:32, alignItems:'center'
            }}>
              <button
                className="btn btn-secondary btn-sm"
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
              >
                ← Prev
              </button>
              <span style={{ color:'#a0a0a0' }}>
                Page {page + 1} of {totalPages}
              </span>
              <button
                className="btn btn-secondary btn-sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage(p => p + 1)}
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}

const styles = {
  card: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:'20px 24px',
    display:'flex', justifyContent:'space-between', gap:20
  },
  cardLeft: { flex:1 },
  cardRight: { textAlign:'right', flexShrink:0 },
  refText: { color:'#a0a0a0', fontSize:12, marginTop:4 },
  infoRow: {
    display:'flex', gap:8, marginTop:8, flexWrap:'wrap'
  },
  info: { color:'#a0a0a0', fontSize:13 },
  seats: { display:'flex', gap:6, flexWrap:'wrap', marginTop:10 },
  seat: {
    background:'rgba(229,9,20,0.15)',
    border:'1px solid rgba(229,9,20,0.3)',
    color:'#e50914', borderRadius:4,
    padding:'2px 10px', fontSize:12, fontWeight:600
  },
  statusBadge: {
    display:'inline-block', border:'1px solid',
    borderRadius:20, padding:'4px 12px',
    fontSize:11, fontWeight:700
  }
}