import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchShowById } from '../store/slices/showSlice'
import {
  lockSeats, setSelectedSeats
} from '../store/slices/bookingSlice'
import Loader from '../components/common/Loader'
import toast from 'react-hot-toast'

export default function SeatSelection() {
  const { showId } = useParams()
  const dispatch   = useDispatch()
  const navigate   = useNavigate()
  const { current: show, loading: showLoading } =
    useSelector((s) => s.shows)
  const {
    selectedSeats, lockLoading, lockInfo
  } = useSelector((s) => s.booking)
  const { isAuthenticated } = useSelector((s) => s.auth)

  useEffect(() => {
    dispatch(fetchShowById(showId))
    dispatch(setSelectedSeats([]))
  }, [showId, dispatch])

  useEffect(() => {
    if (lockInfo) {
      navigate('/payment')
    }
  }, [lockInfo, navigate])

  const toggleSeat = useCallback((seatNum, isBooked) => {
    if (isBooked) return
    dispatch(setSelectedSeats(
      selectedSeats.includes(seatNum)
        ? selectedSeats.filter(s => s !== seatNum)
        : selectedSeats.length >= 10
          ? (toast.error('Max 10 seats allowed'), selectedSeats)
          : [...selectedSeats, seatNum]
    ))
  }, [selectedSeats, dispatch])

  const handleProceed = async () => {
    if (!isAuthenticated) {
      toast.error('Please login to book tickets')
      navigate('/login')
      return
    }
    if (selectedSeats.length === 0) {
      toast.error('Please select at least one seat')
      return
    }
    dispatch(lockSeats({ showId, seatNumbers: selectedSeats }))
  }

  if (showLoading) return <Loader />
  if (!show) return (
    <div className="text-center" style={{ padding:80 }}>
      Show not found
    </div>
  )

  const layout  = show.screen?.seatLayout
  const booked  = show.bookedSeats || []
  const categories = layout?.seatCategories || []

  // Build complete seat map
  const allSeats = categories.flatMap(cat =>
    cat.rows.flatMap(row =>
      Array.from({ length: cat.seatsPerRow }, (_, i) => ({
        id:       `${row}${i + 1}`,
        row,
        col:      i + 1,
        type:     cat.type,
        price:    cat.price,
        color:    cat.colorCode || '#4CAF50',
        isBooked: booked.includes(`${row}${i + 1}`)
      }))
    )
  )

  // Group by row
  const rowMap = allSeats.reduce((acc, s) => {
    if (!acc[s.row]) acc[s.row] = []
    acc[s.row].push(s)
    return acc
  }, {})

  // Calculate totals
  const totalAmount = selectedSeats.reduce((sum, seatId) => {
    const seat = allSeats.find(s => s.id === seatId)
    return sum + (seat?.price || 0)
  }, 0)
  const convFee = Math.round(totalAmount * 0.02 * 100) / 100
  const grandTotal = totalAmount + convFee

  return (
    <div style={{ maxWidth:900, margin:'0 auto', padding:'24px 20px' }}>
      {/* Show info */}
      <div style={styles.showInfo}>
        <div>
          <h2 style={{ fontSize:20, fontWeight:700 }}>
            {show.movieTitle}
          </h2>
          <p style={{ color:'#a0a0a0', fontSize:13, marginTop:4 }}>
            {show.theatreName} • {show.screenName} •{' '}
            {show.showDate} {show.startTime?.toString().slice(0,5)}
          </p>
        </div>
        <div style={{ textAlign:'right' }}>
          <span style={{ background:'#16213e', padding:'4px 12px',
                         borderRadius:6, fontSize:12 }}>
            {show.language} • {show.format}
          </span>
        </div>
      </div>

      {/* Screen indicator */}
      <div style={styles.screen}>
        <div style={styles.screenLine} />
        <span style={styles.screenText}>SCREEN THIS WAY</span>
      </div>

      {/* Legend */}
      <div style={styles.legend}>
        {categories.map(cat => (
          <div key={cat.type} style={styles.legendItem}>
            <div style={{
              ...styles.legendDot,
              background: cat.colorCode || '#4CAF50'
            }} />
            <span style={{ fontSize:12, color:'#a0a0a0' }}>
              {cat.type} — ₹{cat.price}
            </span>
          </div>
        ))}
        <div style={styles.legendItem}>
          <div style={{ ...styles.legendDot, background:'#333' }} />
          <span style={{ fontSize:12, color:'#a0a0a0' }}>Booked</span>
        </div>
        <div style={styles.legendItem}>
          <div style={{
            ...styles.legendDot,
            background:'#e50914',
            border:'2px solid #ff4444'
          }} />
          <span style={{ fontSize:12, color:'#a0a0a0' }}>Selected</span>
        </div>
      </div>

      {/* Seat Grid */}
      <div style={styles.seatGrid}>
        {Object.entries(rowMap).map(([row, seats]) => (
          <div key={row} style={styles.seatRow}>
            <span style={styles.rowLabel}>{row}</span>
            <div style={styles.seatsWrap}>
              {seats.map((seat) => {
                const isSelected = selectedSeats.includes(seat.id)
                const isBooked   = seat.isBooked

                return (
                  <button
                    key={seat.id}
                    onClick={() => toggleSeat(seat.id, isBooked)}
                    title={`${seat.id} — ₹${seat.price} (${seat.type})`}
                    style={{
                      ...styles.seat,
                      background: isBooked
                        ? '#2a2a2a'
                        : isSelected
                        ? '#e50914'
                        : seat.color + '33',
                      border: isBooked
                        ? '1px solid #333'
                        : isSelected
                        ? '2px solid #ff4444'
                        : `1px solid ${seat.color}66`,
                      cursor: isBooked ? 'not-allowed' : 'pointer',
                      opacity: isBooked ? 0.4 : 1
                    }}
                  >
                    {seat.col}
                  </button>
                )
              })}
            </div>
            <span style={styles.rowLabel}>{row}</span>
          </div>
        ))}
      </div>

      {/* Summary & Proceed */}
      {selectedSeats.length > 0 && (
        <div style={styles.summary}>
          <div style={styles.summaryLeft}>
            <p style={{ fontWeight:700 }}>
              {selectedSeats.length} Seat{selectedSeats.length > 1 ? 's' : ''}
            </p>
            <p style={{ color:'#a0a0a0', fontSize:13 }}>
              {selectedSeats.join(', ')}
            </p>
          </div>
          <div style={{ textAlign:'right' }}>
            <p style={{ color:'#a0a0a0', fontSize:12 }}>
              Subtotal: ₹{totalAmount}
            </p>
            <p style={{ color:'#a0a0a0', fontSize:12 }}>
              Convenience fee: ₹{convFee}
            </p>
            <p style={{ fontWeight:700, fontSize:18, color:'#f5c518' }}>
              Total: ₹{grandTotal}
            </p>
          </div>
          <button
            className="btn btn-primary btn-lg"
            onClick={handleProceed}
            disabled={lockLoading}
          >
            {lockLoading ? 'Locking...' : 'Proceed to Pay →'}
          </button>
        </div>
      )}
    </div>
  )
}

const styles = {
  showInfo: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:'16px 20px',
    display:'flex', justifyContent:'space-between',
    alignItems:'center', marginBottom:24
  },
  screen: { textAlign:'center', margin:'24px 0 16px' },
  screenLine: {
    height:4,
    background:'linear-gradient(to right,transparent,#4a9eff,transparent)',
    borderRadius:2, marginBottom:8
  },
  screenText: { color:'#4a9eff', fontSize:11, letterSpacing:3 },
  legend: {
    display:'flex', justifyContent:'center', gap:20,
    marginBottom:24, flexWrap:'wrap'
  },
  legendItem: { display:'flex', alignItems:'center', gap:6 },
  legendDot: { width:14, height:14, borderRadius:3 },
  seatGrid: { marginBottom:24 },
  seatRow: {
    display:'flex', alignItems:'center', gap:8, marginBottom:6
  },
  rowLabel: {
    color:'#666', fontSize:12, fontWeight:600,
    minWidth:20, textAlign:'center'
  },
  seatsWrap: { display:'flex', gap:4, flexWrap:'wrap', flex:1 },
  seat: {
    width:32, height:32, borderRadius:5,
    fontSize:10, fontWeight:600, color:'#fff',
    transition:'all 0.15s', display:'flex',
    alignItems:'center', justifyContent:'center'
  },
  summary: {
    position:'sticky', bottom:0,
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:'16px 24px',
    display:'flex', alignItems:'center',
    justifyContent:'space-between', gap:20,
    boxShadow:'0 -8px 32px rgba(0,0,0,0.4)'
  },
  summaryLeft: { flex:1 }
}