import { useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchBookingById } from '../store/slices/bookingSlice'
import Loader from '../components/common/Loader'
import { FiDownload, FiShare2, FiHome } from 'react-icons/fi'

export default function BookingConfirmation() {
  const { id }   = useParams()
  const dispatch = useDispatch()
  const { current: booking, loading } =
    useSelector((s) => s.booking)

  useEffect(() => { dispatch(fetchBookingById(id)) }, [id, dispatch])

  if (loading) return <Loader />
  if (!booking) return (
    <div className="text-center" style={{ padding:80 }}>
      Booking not found
    </div>
  )

  const isConfirmed = booking.status === 'CONFIRMED'

  return (
    <div style={{ maxWidth:640, margin:'0 auto', padding:'40px 20px' }}>
      {/* Status header */}
      <div style={{
        ...styles.statusBox,
        background: isConfirmed
          ? 'rgba(76,175,80,0.1)'
          : 'rgba(255,152,0,0.1)',
        borderColor: isConfirmed ? '#4caf50' : '#ff9800'
      }}>
        <div style={{ fontSize:56 }}>
          {isConfirmed ? '🎉' : '⏳'}
        </div>
        <h1 style={{ fontSize:24, fontWeight:800, marginTop:12 }}>
          {isConfirmed
            ? 'Booking Confirmed!'
            : 'Booking Pending'}
        </h1>
        <p style={{ color:'#a0a0a0', marginTop:6, fontSize:14 }}>
          {isConfirmed
            ? 'Your tickets are ready. Enjoy the show!'
            : 'Payment processing...'}
        </p>
      </div>

      {/* Booking Reference */}
      <div style={styles.refBox}>
        <p style={{ color:'#a0a0a0', fontSize:12 }}>
          BOOKING REFERENCE
        </p>
        <h2 style={{
          fontFamily:'monospace', fontSize:26,
          letterSpacing:3, color:'#e50914', marginTop:4
        }}>
          {booking.bookingReference}
        </h2>
      </div>

      {/* Ticket */}
      <div style={styles.ticket}>
        <div style={styles.ticketHeader}>
          <h3 style={{ fontSize:20, fontWeight:700 }}>
            {booking.movieTitle}
          </h3>
          <div style={styles.statusBadge}>
            <span style={{
              color: isConfirmed ? '#4caf50' : '#ff9800',
              fontSize:12, fontWeight:700
            }}>
              ● {booking.status}
            </span>
          </div>
        </div>

        <div style={styles.ticketBody}>
          {[
            ['🏟️ Theatre',  booking.theatreName],
            ['🎭 Screen',   booking.screenName],
            ['📅 Date',     booking.showDate],
            ['⏰ Time',     booking.showTime],
            ['🌐 Language', booking.language],
            ['📽️ Format',  booking.format],
          ].map(([label, value]) => (
            <div key={label} style={styles.ticketRow}>
              <span style={{ color:'#a0a0a0', fontSize:13 }}>
                {label}
              </span>
              <span style={{ fontWeight:600, fontSize:14 }}>
                {value || 'N/A'}
              </span>
            </div>
          ))}

          <div style={styles.ticketRow}>
            <span style={{ color:'#a0a0a0', fontSize:13 }}>
              💺 Seats
            </span>
            <div style={{ display:'flex', gap:6, flexWrap:'wrap' }}>
              {booking.seatNumbers?.map(s => (
                <span key={s} style={styles.seatTag}>{s}</span>
              ))}
            </div>
          </div>

          <div style={{
            ...styles.ticketRow,
            borderTop:'2px dashed #2a2a4a',
            paddingTop:12, marginTop:4
          }}>
            <span style={{ fontWeight:700 }}>💰 Total Paid</span>
            <span style={{
              fontWeight:800, fontSize:20, color:'#f5c518'
            }}>
              ₹{booking.grandTotal}
            </span>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div style={styles.actions}>
        <Link to="/" className="btn btn-secondary">
          <FiHome size={16} /> Home
        </Link>
        <Link to="/my-bookings" className="btn btn-secondary">
          My Bookings
        </Link>
        <button
          className="btn btn-primary"
          onClick={() => window.print()}
        >
          <FiDownload size={16} /> Download
        </button>
      </div>

      {/* Note */}
      {isConfirmed && (
        <div style={styles.note}>
          <p style={{ fontSize:13, color:'#a0a0a0', textAlign:'center' }}>
            📧 A confirmation email has been sent to your registered email address.
            Please carry a valid ID along with this booking reference.
          </p>
        </div>
      )}
    </div>
  )
}

const styles = {
  statusBox: {
    border:'2px solid', borderRadius:16, padding:'32px 24px',
    textAlign:'center', marginBottom:20
  },
  refBox: {
    background:'#1a1a2e', border:'2px solid rgba(229,9,20,0.3)',
    borderRadius:12, padding:'16px 24px',
    textAlign:'center', marginBottom:20
  },
  ticket: {
    background:'#1a1a2e', border:'2px dashed #2a2a4a',
    borderRadius:12, overflow:'hidden', marginBottom:20
  },
  ticketHeader: {
    background:'#16213e', padding:'16px 20px',
    display:'flex', justifyContent:'space-between', alignItems:'center'
  },
  statusBadge: {
    background:'rgba(76,175,80,0.1)', borderRadius:20,
    padding:'4px 12px', border:'1px solid rgba(76,175,80,0.3)'
  },
  ticketBody: { padding:'16px 20px' },
  ticketRow: {
    display:'flex', justifyContent:'space-between',
    alignItems:'center', padding:'8px 0',
    borderBottom:'1px solid #2a2a4a'
  },
  seatTag: {
    background:'#e50914', color:'white',
    borderRadius:4, padding:'2px 10px',
    fontSize:12, fontWeight:700
  },
  actions: { display:'flex', gap:12, marginBottom:16 },
  note: {
    background:'#16213e', border:'1px solid #2a2a4a',
    borderRadius:10, padding:'14px 16px'
  }
}