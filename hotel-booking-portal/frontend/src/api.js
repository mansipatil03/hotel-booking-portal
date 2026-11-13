async function call(path, options = {}) {
  let res;
  try {
    res = await fetch('/api' + path, { headers: { 'Content-Type': 'application/json' }, ...options });
  } catch {
    throw new Error('Cannot reach the server. Please check that the backend is running.');
  }
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(data?.message || 'Something went wrong. Please try again.');
  return data;
}

const qs = (params) => {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => v && p.set(k, v));
  const s = p.toString();
  return s ? '?' + s : '';
};

export const api = {
  searchHotels: (location, checkIn, checkOut) => call('/hotels' + qs({ location, checkIn, checkOut })),
  hotel: (id, checkIn, checkOut) => call(`/hotels/${id}` + qs({ checkIn, checkOut })),
  book: (body) => call('/bookings', { method: 'POST', body: JSON.stringify(body) }),
  getBooking: (ref, email) => call(`/bookings/${encodeURIComponent(ref)}` + qs({ email })),
  cancel: (ref, email) => call(`/bookings/${encodeURIComponent(ref)}/cancel` + qs({ email }), { method: 'POST' }),
};
