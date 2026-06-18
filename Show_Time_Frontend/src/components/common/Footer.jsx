import { Link } from 'react-router-dom'
import { FiFilm } from 'react-icons/fi'

export default function Footer() {
  return (
    <footer style={styles.footer}>
      <div className="container">
        <div style={styles.grid}>
          <div>
            <div style={styles.logo}>
              <FiFilm size={20} color="#e50914" />
              <span style={{ fontSize:18, fontWeight:700 }}>
                Show<span style={{ color:'#e50914' }}>Time</span>
              </span>
            </div>
            <p style={styles.desc}>
              India's favourite movie ticket booking platform.
              Book tickets for the latest movies in your city.
            </p>
          </div>
          <div>
            <h4 style={styles.heading}>Quick Links</h4>
            <div style={styles.links}>
              <Link to="/movies" style={styles.link}>Movies</Link>
              <Link to="/my-bookings" style={styles.link}>My Bookings</Link>
              <Link to="/profile" style={styles.link}>Profile</Link>
            </div>
          </div>
          <div>
            <h4 style={styles.heading}>Support</h4>
            <div style={styles.links}>
              <span style={styles.link}>Help Center</span>
              <span style={styles.link}>Contact Us</span>
              <span style={styles.link}>Privacy Policy</span>
            </div>
          </div>
        </div>
        <div style={styles.bottom}>
          <p style={{ color:'#666', fontSize:13 }}>
            © 2024 ShowTime. All rights reserved.
          </p>
          <p style={{ color:'#666', fontSize:13 }}>
            Built with ❤️ using Spring Boot + React
          </p>
        </div>
      </div>
    </footer>
  )
}

const styles = {
  footer: {
    background:'#0d0d1a',
    borderTop:'1px solid #2a2a4a',
    padding:'40px 0 20px'
  },
  grid: {
    display:'grid',
    gridTemplateColumns:'2fr 1fr 1fr',
    gap:40, marginBottom:32
  },
  logo: {
    display:'flex', alignItems:'center',
    gap:8, marginBottom:12
  },
  desc: { color:'#666', fontSize:13, lineHeight:1.8 },
  heading: { color:'#fff', fontSize:14, fontWeight:600, marginBottom:12 },
  links: { display:'flex', flexDirection:'column', gap:8 },
  link: { color:'#666', fontSize:13, cursor:'pointer', transition:'color 0.2s' },
  bottom: {
    borderTop:'1px solid #2a2a4a', paddingTop:16,
    display:'flex', justifyContent:'space-between'
  }
}