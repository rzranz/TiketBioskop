/**
 * CineMax TIX - Application State & Configuration
 * Clean Code: Centralized State Management (Single Source of Truth)
 */

const AppState = {
  // Region & Location
  currentCity: 'BANDUNG',
  selectedBrand: 'ALL',

  // Authentication State
  currentUser: {
    name: 'Budi Santoso',
    phone: '0812-3456-7890',
    email: 'budi.santoso@example.com'
  },
  isAdmin: false,

  // Catalogs
  movies: [],
  cinemas: [],
  foodItems: [],
  activeBookings: [],

  // Current Booking Session
  selectedMovie: null,
  selectedCinema: null,
  selectedStudio: null,
  selectedShowtime: null,
  selectedShowtimeId: null,
  selectedShowtimeDate: '10 Nov',
  selectedSeats: [], // Array of seat objects { id, seatCode, price, tier }
  selectedFoods: {}, // foodId -> qty map
  
  // Concurrency & Seat Hold TTL
  seatHoldTimer: null,
  seatHoldSeconds: 300, // 5 minutes standard hold

  // Selected Ticket for Modal View
  viewingTicket: null
};

// Global export for vanilla ES-friendly modules
window.AppState = AppState;
