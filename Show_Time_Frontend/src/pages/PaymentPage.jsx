import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import {
  initiatePayment, verifyPayment, clearPayment
} from '../store/slices/paymentSlice'
import { createBooking, clearBooking } from '../store/slices/bookingSlice'
import Loader from '../components/common/Loader'
import toast from 'react-hot-toast'
import { FiCreditCard, FiSmartphone, FiGlobe, FiLock } from 'react-icons/fi'

const METHODS = [
  { id:'UPI',         icon:<FiSmartphone size={20} />, label:'UPI' },
  { id:'CARD',        icon:<FiCreditCard size={20} />, label:'Debit/Credit Card' },
  { id:'NET_BANKING', icon:<FiGlobe size={20} />,      label:'Net Banking' },
  { id:'WALLET',      icon:<FiSmartphone size={20} />, label:'Wallet' }
]

export default function PaymentPage() {
  const dispatch  = useDispatch()
  const navigate  = useNavigate()

  const { lockInfo, selectedSeats, current: booking } =
    useSelector((s) => s.booking)
  const { current: payment, loading: payLoading } =
    useSelector((s) => s.payment)
  const show = useSelector((s) => s.shows.current)

  const [method,  setMethod]  = useState('UPI')
  const [upiId,   setUpiId]   = useState('')
  const [step,    setStep]     = useState('details')
  // steps: details → processing → result

  // Redirect if no lock info
  useEffect(() => {
    if (!lockInfo) {
      toast.error('Session expired. Please select seats again.')
      navigate(-1)
    }
  }, [lockInfo, navigate])

  const handlePayNow = async () => {
    if (!lockInfo) return

    setStep('processing')

    try {
      // Step 1: Create booking
      let bookingId = booking?.id
      if (!bookingId) {
        const bookingData = {
          showId:      lockInfo.showId,
          seatNumbers: lockInfo.lockedSeats,
          movieTitle:  show?.movieTitle,
          theatreName: show?.theatreName,
          screenName:  show?.screenName,
          showDate:    show?.showDate?.toString(),
          showTime:    show?.startTime?.toString(),
          language:    show?.language,
          format:      show?.format
        }
        const result = await dispatch(createBooking(bookingData))
        if (result.error) {
          setStep('details')
          return
        }
        bookingId = result.payload.id
      }

      // Step 2: Initiate payment
      const initResult = await dispatch(initiatePayment({
        bookingId,
        amount:       lockInfo.grandTotal,
        paymentMethod: method,
        upiId:        method === 'UPI' ? upiId : undefined
      }))

      if (initResult.error) {
        setStep('details')
        return
      }

      const paymentId   = initResult.payload.id
      const orderId     = initResult.payload.gatewayOrderId

      // Step 3: Verify payment (mock — auto-triggers after 1.5s)
      await new Promise(r => setTimeout(r, 1500))

      const verifyResult = await dispatch(verifyPayment({
        paymentId,
        gatewayPaymentId: `PAY_MOCK_${Date.now()}`,
        gatewayOrderId:   orderId,
        gatewaySignature: `SIG_MOCK_${Date.now()}`
      }))

      if (!verifyResult.error) {
        setStep('result')
        const confirmedBookingId =
          verifyResult.payload.bookingId || bookingId
        setTimeout(() => {
          navigate(`/booking/confirmation/${confirmedBookingId}`)
          dispatch(clearBooking())
          dispatch(clearPayment())
        }, 2000)
      } else {
        setStep('details')
      }

    } catch {
      setStep('details')
      toast.error('An unexpected error occurred')
    }
  }

  if (!lockInfo) return null

  return (
    <div style={styles.page}>
      <div style={styles.container}>

        {/* Order Summary */}
        <div style={styles.summary}>
          <h3 style={{ fontSize:16, fontWeight:700, marginBottom:16 }}>
            Order Summary
          </h3>
          <div style={styles.summaryRow}>
            <span style={{ color:'#a0a0a0' }}>Seats</span>
            <span>{lockInfo.lockedSeats?.join(', ')}</span>
          </div>
          <div style={styles.summaryRow}>
            <span style={{ color:'#a0a0a0' }}>Subtotal</span>
            <span>₹{lockInfo.totalAmount}</span>
          </div>
          <div style={styles.summaryRow}>
            <span style={{ color:'#a0a0a0' }}>Convenience Fee</span>
            <span>₹{lockInfo.convenienceFee}</span>
          </div>
          <div style={{
            ...styles.summaryRow,
            borderTop:'1px solid #2a2a4a',
            paddingTop:12, marginTop:8
          }}>
            <span style={{ fontWeight:700, fontSize:16 }}>Total</span>
            <span style={{
              fontWeight:800, fontSize:20, color:'#f5c518'
            }}>
              ₹{lockInfo.grandTotal}
            </span>
          </div>

          {/* Timer */}
          <div style={styles.timer}>
            <span style={{ fontSize:12, color:'#a0a0a0' }}>
              ⏱ Seats locked for
            </span>
            <span style={{ color:'#e50914', fontWeight:700 }}>
              10:00 min
            </span>
          </div>
        </div>

        {/* Payment Form */}
        <div style={styles.paymentBox}>
          {step === 'processing' ? (
            <div style={{ padding:'60px 0', textAlign:'center' }}>
              <div style={styles.spinner} />
              <p style={{ color:'#a0a0a0', marginTop:16 }}>
                Processing your payment...
              </p>
              <p style={{ color:'#666', fontSize:12, marginTop:8 }}>
                Please don't close this window
              </p>
            </div>
          ) : step === 'result' ? (
            <div style={{ padding:'60px 0', textAlign:'center' }}>
              <div style={{ fontSize:64 }}>✅</div>
              <h3 style={{ marginTop:16, color:'#4caf50' }}>
                Payment Successful!
              </h3>
              <p style={{ color:'#a0a0a0', marginTop:8 }}>
                Redirecting to confirmation...
              </p>
            </div>
          ) : (
            <>
              <h3 style={{ fontSize:16, fontWeight:700, marginBottom:16 }}>
                Payment Method
              </h3>

              {/* Method selector */}
              <div style={styles.methods}>
                {METHODS.map(m => (
                  <button
                    key={m.id}
                    onClick={() => setMethod(m.id)}
                    style={{
                      ...styles.methodBtn,
                      ...(method === m.id ? styles.methodBtnActive : {})
                    }}
                  >
                    {m.icon}
                    <span>{m.label}</span>
                  </button>
                ))}
              </div>

              {/* UPI input */}
              {method === 'UPI' && (
                <div className="form-group" style={{ marginTop:20 }}>
                  <label>UPI ID</label>
                  <input
                    placeholder="yourname@paytm / upi"
                    value={upiId}
                    onChange={(e) => setUpiId(e.target.value)}
                    style={styles.input}
                  />
                </div>
              )}

              {method === 'CARD' && (
                <div style={{ marginTop:20, color:'#a0a0a0',
                              fontSize:13, textAlign:'center' }}>
                  <p>🔒 Card details are encrypted</p>
                  <p style={{ marginTop:4 }}>
                    (Mock mode — no real card needed)
                  </p>
                </div>
              )}

              <button
                className="btn btn-primary btn-lg"
                onClick={handlePayNow}
                disabled={payLoading}
                style={{
                  width:'100%', marginTop:24,
                  justifyContent:'center'
                }}
              >
                <FiLock size={16} />
                Pay ₹{lockInfo.grandTotal} Securely
              </button>

              <p style={{
                textAlign:'center', color:'#666',
                fontSize:11, marginTop:12
              }}>
                🔒 256-bit SSL encrypted • Mock payment (safe)
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: {
    minHeight:'calc(100vh - 64px)',
    display:'flex', alignItems:'center', justifyContent:'center',
    padding:'32px 20px'
  },
  container: {
    display:'grid', gridTemplateColumns:'1fr 1.5fr',
    gap:24, width:'100%', maxWidth:840
  },
  summary: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:24, height:'fit-content'
  },
  summaryRow: {
    display:'flex', justifyContent:'space-between',
    padding:'8px 0', fontSize:14
  },
  timer: {
    display:'flex', justifyContent:'space-between',
    marginTop:16, padding:'10px 14px',
    background:'rgba(229,9,20,0.08)',
    border:'1px solid rgba(229,9,20,0.2)',
    borderRadius:8, fontSize:13
  },
  paymentBox: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:12, padding:24
  },
  methods: {
    display:'grid', gridTemplateColumns:'1fr 1fr', gap:10
  },
  methodBtn: {
    display:'flex', alignItems:'center', gap:10,
    padding:'12px 16px', background:'#16213e',
    border:'1px solid #2a2a4a', borderRadius:10,
    color:'#a0a0a0', cursor:'pointer', fontSize:13,
    transition:'all 0.2s'
  },
  methodBtnActive: {
    border:'2px solid #e50914', color:'#fff',
    background:'rgba(229,9,20,0.08)'
  },
  input: {
    width:'100%', background:'#16213e',
    border:'1px solid #2a2a4a', borderRadius:10,
    padding:'12px 16px', color:'#fff', fontSize:14
  },
  spinner: {
    width:48, height:48,
    border:'3px solid #2a2a4a',
    borderTop:'3px solid #e50914',
    borderRadius:'50%',
    animation:'spin 0.8s linear infinite',
    margin:'0 auto'
  }
}