import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useSearchParams } from 'react-router-dom'
import {
  fetchMovies, searchMovies, fetchGenres
} from '../store/slices/movieSlice'
import MovieCard from '../components/ui/MovieCard'
import Loader from '../components/common/Loader'
import { FiSearch, FiFilter } from 'react-icons/fi'
import axiosInstance from '../utils/axiosInstance'

export default function MovieListing() {
  const dispatch = useDispatch()
  const [searchParams] = useSearchParams()
  const {
    list, loading, totalPages, currentPage, genres
  } = useSelector((s) => s.movies)

  const [query,    setQuery]    = useState(searchParams.get('q') || '')
  const [genre,    setGenre]    = useState('')
  const [language, setLanguage] = useState('')
  const [page,     setPage]     = useState(0)
  const [languages, setLanguages] = useState([])

  useEffect(() => {
    dispatch(fetchGenres())
    axiosInstance.get('/movies/languages')
      .then(r => setLanguages(r.data.data || []))
      .catch(() => {})
  }, [dispatch])

  useEffect(() => {
    if (query) {
      dispatch(searchMovies({ q:query, page, size:12 }))
    } else if (genre || language) {
      axiosInstance.get(
        `/movies/filter?genre=${genre}&language=${language}&page=${page}&size=12`
      )
    } else {
      dispatch(fetchMovies({ page, size:12 }))
    }
  }, [dispatch, query, genre, language, page])

  return (
    <div className="container" style={{ padding:'32px 20px' }}>
      <h1 style={{ fontSize:26, fontWeight:700, marginBottom:24 }}>
        🎬 Movies
      </h1>

      {/* Filters */}
      <div style={styles.filters}>
        <div style={styles.searchBox}>
          <FiSearch size={16} color="#a0a0a0" />
          <input
            placeholder="Search movies..."
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(0) }}
            style={styles.searchInput}
          />
        </div>

        <select
          value={genre}
          onChange={(e) => { setGenre(e.target.value); setPage(0) }}
          style={styles.select}
        >
          <option value="">All Genres</option>
          {genres.map(g => <option key={g} value={g}>{g}</option>)}
        </select>

        <select
          value={language}
          onChange={(e) => { setLanguage(e.target.value); setPage(0) }}
          style={styles.select}
        >
          <option value="">All Languages</option>
          {languages.map(l => <option key={l} value={l}>{l}</option>)}
        </select>

        {(genre || language || query) && (
          <button
            className="btn btn-secondary btn-sm"
            onClick={() => {
              setQuery(''); setGenre(''); setLanguage(''); setPage(0)
            }}
          >
            Clear Filters
          </button>
        )}
      </div>

      {/* Results */}
      {loading ? (
        <Loader />
      ) : list.length === 0 ? (
        <div className="text-center" style={{ padding:'60px 0' }}>
          <p style={{ fontSize:48 }}>🎭</p>
          <p style={{ color:'#a0a0a0', marginTop:16 }}>
            No movies found
          </p>
        </div>
      ) : (
        <>
          <div style={styles.grid}>
            {list.map(movie => (
              <MovieCard key={movie.id} movie={movie} />
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div style={styles.pagination}>
              <button
                className="btn btn-secondary btn-sm"
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
              >
                ← Prev
              </button>
              <span style={{ color:'#a0a0a0', fontSize:14 }}>
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
  filters: {
    display:'flex', flexWrap:'wrap', gap:12, marginBottom:28,
    alignItems:'center'
  },
  searchBox: {
    display:'flex', alignItems:'center', gap:10,
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:10, padding:'10px 16px', flex:1, minWidth:220
  },
  searchInput: {
    background:'transparent', border:'none', color:'#fff',
    fontSize:14, flex:1
  },
  select: {
    background:'#1a1a2e', border:'1px solid #2a2a4a',
    borderRadius:10, padding:'10px 16px', color:'#fff', fontSize:14
  },
  grid: {
    display:'grid',
    gridTemplateColumns:'repeat(auto-fill, minmax(200px, 1fr))',
    gap:20
  },
  pagination: {
    display:'flex', justifyContent:'center',
    alignItems:'center', gap:16, marginTop:40
  }
}