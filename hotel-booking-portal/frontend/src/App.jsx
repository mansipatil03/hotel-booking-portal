import { useEffect, useState } from 'react';
import { api } from './api';

const localToday = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
const money = (n) => '₹' + Number(n).toLocaleString('en-IN');
const fmtDate = (d) => new Date(d + 'T00:00:00').toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });

export default function App() {
  const [tab, setTab] = useState('hotels');
  return (
    <div className="page">
      <header>
        <h1>🏨 Hotel Booking Portal</h1>
        <nav>
          <button className={tab === 'hotels' ? 'active' : ''} onClick={() => setTab('hotels')}>Find a hotel</button>
          <button className={tab === 'manage' ? 'active' : ''} onClick={() => setTab('manage')}>Manage booking</button>
        </nav>
      </header>
      {tab === 'hotels' ? <Hotels /> : <Manage />}
    </div>
  );
}

function Hotels() {
  const [location, setLocation] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [hotels, setHotels] = useState([]);
  const [selected, setSelected] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const search = async (e, ci = checkIn, co = checkOut) => {
    e?.preventDefault();
    setError('');
    setLoading(true);
    try { setHotels(await api.searchHotels(location, ci, co)); }
    catch (err) { setError(err.message); setHotels([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { search(); }, []); // eslint-disable-line

  if (selected) {
    return (
      <HotelDetail
        id={selected} checkIn={checkIn} checkOut={checkOut}
        onDates={(ci, co) => { setCheckIn(ci); setCheckOut(co); }}
        onBack={() => { setSelected(null); search(); }}
      />
    );
  }

  return (
    <>
      <form className="card search" onSubmit={search}>
        <label>Location<input value={location} onChange={(e) => setLocation(e.target.value)} placeholder="e.g. Goa" /></label>
        <label>Check-in<input type="date" min={localToday()} value={checkIn} onChange={(e) => setCheckIn(e.target.value)} /></label>
        <label>Check-out<input type="date" min={checkIn || localToday()} value={checkOut} onChange={(e) => setCheckOut(e.target.value)} /></label>
        <button className="primary" disabled={loading}>{loading ? 'Searching…' : 'Search'}</button>
      </form>
      {error && <p className="error">{error}</p>}
      {!loading && !error && hotels.length === 0 && <p className="muted">No hotels found for that location.</p>}
      <div className="grid">
        {hotels.map((h) => (
          <div className="card" key={h.id}>
            <h3>{h.name}</h3>
            <p className="muted">📍 {h.location}</p>
            <p>{h.description}</p>
            <p>{h.amenities.map((a) => <span className="tag" key={a}>{a}</span>)}</p>
            <p className="price">{money(h.pricePerNight)} <small>/ night</small></p>
            {h.availableRooms != null && (
              <p className={h.availableRooms > 0 ? 'ok' : 'error'}>
                {h.availableRooms > 0 ? `${h.availableRooms} room(s) left · total ${money(h.totalAmount)}` : 'Sold out for these dates'}
              </p>
            )}
            <button className="primary" onClick={() => setSelected(h.id)}>View details</button>
          </div>
        ))}
      </div>
    </>
  );
}

function HotelDetail({ id, checkIn, checkOut, onDates, onBack }) {
  const [hotel, setHotel] = useState(null);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ guestName: '', email: '' });
  const [submitting, setSubmitting] = useState(false);
  const [booking, setBooking] = useState(null);

  const load = async () => {
    setError('');
    try { setHotel(await api.hotel(id, checkIn, checkOut)); }
    catch (err) { setError(err.message); }
  };
  useEffect(() => { load(); }, [id, checkIn, checkOut]); // eslint-disable-line

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try { setBooking(await api.book({ hotelId: id, checkIn, checkOut, ...form })); }
    catch (err) { setError(err.message); load(); }
    finally { setSubmitting(false); }
  };

  if (booking) return <Confirmation booking={booking} onDone={onBack} />;

  return (
    <div className="card">
      <button onClick={onBack}>← Back</button>
      {hotel && (
        <>
          <h2>{hotel.name}</h2>
          <p className="muted">📍 {hotel.location}</p>
          <p>{hotel.description}</p>
          <p>{hotel.amenities.map((a) => <span className="tag" key={a}>{a}</span>)}</p>
          <p className="price">{money(hotel.pricePerNight)} <small>/ night</small></p>
        </>
      )}
      <div className="row">
        <label>Check-in<input type="date" min={localToday()} value={checkIn} onChange={(e) => onDates(e.target.value, checkOut)} /></label>
        <label>Check-out<input type="date" min={checkIn || localToday()} value={checkOut} onChange={(e) => onDates(checkIn, e.target.value)} /></label>
      </div>
      {error && <p className="error">{error}</p>}
      {hotel && !checkIn && !checkOut && <p className="muted">Select your dates to see availability and the total price.</p>}

      {hotel?.availableRooms === 0 && (
        <div className="suggest">
          <strong>Sold out for these dates.</strong>
          {hotel.alternatives.length > 0 ? (
            <>
              <p>Same length of stay is available on:</p>
              {hotel.alternatives.map((a) => (
                <button key={a.checkIn} onClick={() => onDates(a.checkIn, a.checkOut)}>
                  {fmtDate(a.checkIn)} → {fmtDate(a.checkOut)} · {money(a.totalAmount)}
                </button>
              ))}
            </>
          ) : <p>No alternatives within two weeks of your dates.</p>}
        </div>
      )}

      {hotel?.availableRooms > 0 && (
        <form onSubmit={submit}>
          <p className="ok">{hotel.availableRooms} room(s) available</p>
          <label>Your name<input required value={form.guestName} onChange={(e) => setForm({ ...form, guestName: e.target.value })} /></label>
          <label>Email<input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
          <p className="total">{hotel.nights} night(s) × {money(hotel.pricePerNight)} = <strong>{money(hotel.totalAmount)}</strong></p>
          <button className="primary" disabled={submitting}>{submitting ? 'Booking…' : 'Confirm booking'}</button>
        </form>
      )}
    </div>
  );
}

function Confirmation({ booking, onDone }) {
  return (
    <div className="card">
      <h2>✅ Booking confirmed</h2>
      <p>Your booking reference is <strong className="ref">{booking.reference}</strong>. Keep it with your email to view or cancel the booking.</p>
      <BookingDetails booking={booking} />
      <button className="primary" onClick={onDone}>Done</button>
    </div>
  );
}

function BookingDetails({ booking }) {
  return (
    <ul className="details">
      <li>Hotel: <strong>{booking.hotelName}</strong></li>
      <li>Guest: {booking.guestName} ({booking.email})</li>
      <li>Stay: {fmtDate(booking.checkIn)} → {fmtDate(booking.checkOut)} ({booking.nights} night(s))</li>
      <li>Total: <strong>{money(booking.totalAmount)}</strong></li>
      <li>Status: <strong className={booking.status === 'CONFIRMED' ? 'ok' : 'error'}>{booking.status}</strong></li>
    </ul>
  );
}

function Manage() {
  const [reference, setReference] = useState('');
  const [email, setEmail] = useState('');
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const lookup = async (e) => {
    e.preventDefault();
    setError(''); setMessage(''); setBooking(null);
    try { setBooking(await api.getBooking(reference, email)); }
    catch (err) { setError(err.message); }
  };

  const cancel = async () => {
    if (!window.confirm('Cancel this booking?')) return;
    setError('');
    try { setBooking(await api.cancel(reference, email)); setMessage('Your booking has been cancelled.'); }
    catch (err) { setError(err.message); }
  };

  return (
    <div className="card">
      <h2>Find your booking</h2>
      <form onSubmit={lookup}>
        <label>Booking reference<input required value={reference} onChange={(e) => setReference(e.target.value)} placeholder="BK-XXXXXXXX" /></label>
        <label>Email<input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} /></label>
        <button className="primary">Find booking</button>
      </form>
      {error && <p className="error">{error}</p>}
      {message && <p className="ok">{message}</p>}
      {booking && (
        <>
          <BookingDetails booking={booking} />
          {booking.status === 'CONFIRMED' && <button className="danger" onClick={cancel}>Cancel booking</button>}
        </>
      )}
    </div>
  );
}
