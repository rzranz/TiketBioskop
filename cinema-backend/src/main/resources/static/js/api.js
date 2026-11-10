/**
 * CineMax TIX - API Service Client Layer
 * Clean Code: Separates Network Communication from Presentation Components
 */

const Api = {
  /**
   * Internal generic request handler with error wrapping
   */
  async request(endpoint, options = {}) {
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    try {
      const response = await fetch(endpoint, {
        ...options,
        headers: {
          ...defaultHeaders,
          ...(options.headers || {})
        }
      });

      const json = await response.json().catch(() => ({
        success: false,
        message: 'Respon server tidak valid'
      }));

      if (!response.ok || !json.success) {
        throw new Error(json.message || `HTTP ${response.status}: Permintaan gagal`);
      }

      return json.data;
    } catch (error) {
      console.error(`[API Error] ${endpoint}:`, error.message);
      throw error;
    }
  },

  // Movie Endpoints
  movies: {
    getAll: () => Api.request('/api/movies'),
    getComingSoon: () => Api.request('/api/movies/coming-soon'),
    getById: (id) => Api.request(`/api/movies/${id}`),
    create: (movieData) => Api.request('/api/movies', {
      method: 'POST',
      body: JSON.stringify(movieData)
    })
  },

  // Cinema & Studio Endpoints
  cinemas: {
    getAll: (city = '') => Api.request(`/api/cinemas${city ? '?city=' + encodeURIComponent(city) : ''}`),
    getCities: () => Api.request('/api/cinemas/cities'),
    create: (cinemaData) => Api.request('/api/cinemas', {
      method: 'POST',
      body: JSON.stringify(cinemaData)
    }),
    getStudios: (cinemaId) => Api.request(`/api/cinemas/${cinemaId}/studios`),
    createStudio: (cinemaId, studioData) => Api.request(`/api/cinemas/${cinemaId}/studios`, {
      method: 'POST',
      body: JSON.stringify(studioData)
    })
  },

  // Showtime Endpoints
  showtimes: {
    get: (params = {}) => {
      const query = new URLSearchParams();
      if (params.movieId) query.append('movieId', params.movieId);
      if (params.cinemaId) query.append('cinemaId', params.cinemaId);
      const qStr = query.toString();
      return Api.request(`/api/cinemas/showtimes${qStr ? '?' + qStr : ''}`);
    },
    create: (showtimeData) => Api.request('/api/cinemas/showtimes', {
      method: 'POST',
      body: JSON.stringify(showtimeData)
    })
  },

  // Seat Inventory & TTL Hold
  seats: {
    getSeats: (showtimeId) => Api.request(`/api/showtimes/${showtimeId}/seats`),
    hold: (showtimeId, seatCodes, customerName) => Api.request(`/api/showtimes/${showtimeId}/hold`, {
      method: 'POST',
      body: JSON.stringify({ showtimeId, seatCodes, customerName })
    }),
    release: (showtimeId, seatCodes, customerName) => Api.request(`/api/showtimes/${showtimeId}/release`, {
      method: 'POST',
      body: JSON.stringify({ showtimeId, seatCodes, customerName })
    })
  },

  // Concession & Food Endpoints
  foods: {
    getAll: () => Api.request('/api/foods')
  },

  // Booking & Transaction Endpoints
  bookings: {
    getAll: () => Api.request('/api/v2/bookings'),
    create: (bookingPayload) => Api.request('/api/v2/bookings', {
      method: 'POST',
      body: JSON.stringify(bookingPayload)
    }),
    claimTicket: (bookingCode, claimCode) => Api.request('/api/v2/bookings/claim-ticket', {
      method: 'POST',
      body: JSON.stringify({ bookingCode, claimCode })
    }),
    claimFood: (bookingCode, claimCode) => Api.request('/api/v2/bookings/claim-food', {
      method: 'POST',
      body: JSON.stringify({ bookingCode, claimCode })
    })
  },

  // Authentication Endpoints
  auth: {
    login: (username, password) => Api.request('/api/users/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    })
  }
};

window.Api = Api;

