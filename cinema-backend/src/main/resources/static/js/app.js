/**
 * CineMax TIX - Main Application Orchestrator & Bootstrapper
 * Clean Code: Initializes dependencies, coordinates navigation routing, and loads catalogs
 */

const App = {
  async init() {
    // 1. Splash Screen Micro-Interaction Sequence
    const splashProgress = document.getElementById('splashProgress');
    if (splashProgress) splashProgress.style.width = '100%';

    setTimeout(() => {
      const splashScreen = document.getElementById('splashScreen');
      if (splashScreen) {
        splashScreen.style.opacity = '0';
        setTimeout(() => splashScreen.remove(), 700);
      }
    }, 900);

    // 2. Fetch Initial Catalogs concurrently
    await Promise.all([
      this.fetchMovies(),
      this.fetchCinemas(),
      this.fetchFoods(),
      this.fetchActiveBookings()
    ]);

    // 3. Bind search input listener
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
      searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
    }

    // 4. Close dropdown on outside click
    document.addEventListener('click', (e) => {
      const cityBtn = document.getElementById('cityDropdownBtn');
      const cityMenu = document.getElementById('cityDropdown');
      if (cityBtn && cityMenu && !cityBtn.contains(e.target) && !cityMenu.contains(e.target)) {
        cityMenu.classList.add('hidden');
      }
    });

    Utils.showToast('Selamat datang di CineMax TIX!', 'info');
  },

  async fetchMovies() {
    try {
      AppState.movies = await Api.movies.getAll();
      MovieComponent.renderNowShowing();

      const comingSoonMovies = await Api.movies.getComingSoon();
      MovieComponent.renderComingSoon(comingSoonMovies);
    } catch (e) {
      Utils.showToast('Gagal memuat katalog film.', 'error');
    }
  },

  async fetchCinemas() {
    try {
      AppState.cinemas = await Api.cinemas.getAll(AppState.currentCity);
      this.renderGeneralCinemasTab();
    } catch (e) {
      Utils.showToast('Gagal memuat daftar bioskop.', 'error');
    }
  },

  async fetchFoods() {
    try {
      AppState.foodItems = await Api.foods.getAll();
      this.renderFoodTab();
    } catch (e) {
      Utils.showToast('Gagal memuat menu makanan & minuman.', 'error');
    }
  },

  async fetchActiveBookings() {
    try {
      AppState.activeBookings = await Api.bookings.getAll();
      TicketsComponent.renderActiveTickets();
    } catch (e) {
      console.error('Failed to load bookings:', e);
    }
  },

  renderGeneralCinemasTab() {
    const container = document.getElementById('cinemasList');
    if (!container) return;
    container.innerHTML = '';

    (AppState.cinemas || []).forEach(c => {
      const item = document.createElement('div');
      item.className = 'cinema-card p-3.5 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-between shadow';
      item.innerHTML = `
        <div>
          <div class="flex items-center gap-2">
            <span class="text-[10px] font-bold px-1.5 py-0.5 rounded bg-slate-800 text-sky-400 border border-slate-700">${c.brand}</span>
            <h4 class="font-bold text-xs text-white">${c.name}</h4>
          </div>
          <p class="text-[10px] text-slate-400 mt-1">${c.address}</p>
        </div>
        <button onclick="App.switchTab('home')" class="btn-spring px-3 py-1.5 rounded-lg bg-sky-500 text-slate-950 font-bold text-xs">Pilih Film</button>
      `;
      container.appendChild(item);
    });
  },

  renderFoodTab() {
    const container = document.getElementById('foodList');
    if (!container) return;
    container.innerHTML = '';

    (AppState.foodItems || []).forEach(f => {
      const item = document.createElement('div');
      item.className = 'flex items-center justify-between p-3 rounded-2xl bg-slate-950 border border-slate-800';
      item.innerHTML = `
        <div class="flex items-center gap-3">
          <img src="${f.imageUrl}" onerror="handleImgError(this, '${f.name}')" class="w-16 h-16 rounded-xl object-cover shadow bg-slate-900">
          <div>
            <span class="text-[9px] font-bold uppercase text-amber-400 bg-amber-500/10 px-1.5 py-0.5 rounded">${f.category}</span>
            <h4 class="font-bold text-xs text-white mt-1">${f.name}</h4>
            <p class="text-[10px] text-slate-400 line-clamp-1">${f.description}</p>
            <span class="text-xs font-bold text-sky-400 font-mono mt-1 block">${Utils.formatRupiah(f.price)}</span>
          </div>
        </div>
        <button onclick="Utils.showToast('Item ${f.name} dapat Anda tambahkan saat memilih kursi film!', 'info')" class="btn-spring px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-200">
          Pesan
        </button>
      `;
      container.appendChild(item);
    });
  },

  switchTab(tab) {
    document.getElementById('tabHome').classList.add('hidden');
    document.getElementById('tabCinemas').classList.add('hidden');
    document.getElementById('tabTickets').classList.add('hidden');
    document.getElementById('tabFoods').classList.add('hidden');

    document.querySelectorAll('.nav-btn').forEach(btn => {
      btn.classList.remove('text-sky-400');
      btn.classList.add('text-slate-400');
    });

    if (tab === 'home') {
      document.getElementById('tabHome').classList.remove('hidden');
      document.getElementById('navHome').classList.add('text-sky-400');
    } else if (tab === 'cinemas') {
      document.getElementById('tabCinemas').classList.remove('hidden');
      document.getElementById('navCinemas').classList.add('text-sky-400');
      this.renderGeneralCinemasTab();
    } else if (tab === 'tickets') {
      document.getElementById('tabTickets').classList.remove('hidden');
      document.getElementById('navTickets').classList.add('text-sky-400');
      this.fetchActiveBookings();
    } else if (tab === 'foods') {
      document.getElementById('tabFoods').classList.remove('hidden');
      document.getElementById('navFoods').classList.add('text-sky-400');
    }
  },

  toggleCityDropdown() {
    const el = document.getElementById('cityDropdown');
    if (el) el.classList.toggle('hidden');
  },

  async selectCity(city) {
    AppState.currentCity = city;
    const label = document.getElementById('selectedCityLabel');
    if (label) label.innerText = city;

    const dropdown = document.getElementById('cityDropdown');
    if (dropdown) dropdown.classList.add('hidden');

    Utils.showToast(`Lokasi bioskop diubah ke: ${city}`, 'info');
    await this.fetchCinemas();
  },

  filterBrand(brand) {
    AppState.selectedBrand = brand;
    document.querySelectorAll('.brand-pill').forEach(b => {
      b.className = 'brand-pill btn-spring px-4 py-1.5 rounded-full text-xs font-bold border border-slate-800 text-slate-400 hover:border-slate-700';
    });

    const activeEl = document.getElementById(`pill-${brand}`);
    if (activeEl) {
      activeEl.className = 'brand-pill btn-spring px-4 py-1.5 rounded-full text-xs font-bold bg-sky-500 text-slate-950 border-transparent';
    }

    if (brand === 'ALL') {
      MovieComponent.renderNowShowing();
    } else {
      const filtered = AppState.movies.filter(m => (m.genre || '').toLowerCase().includes(brand.toLowerCase()));
      MovieComponent.renderNowShowing(filtered);
    }
  },

  handleSearch(query) {
    const q = (query || '').toLowerCase().trim();
    if (!q) {
      MovieComponent.renderNowShowing();
      return;
    }

    const filtered = AppState.movies.filter(m =>
      (m.title || '').toLowerCase().includes(q) ||
      (m.genre || '').toLowerCase().includes(q)
    );
    MovieComponent.renderNowShowing(filtered);
  }
};

window.App = App;
window.switchTab = (tab) => App.switchTab(tab);
window.toggleCityDropdown = () => App.toggleCityDropdown();
window.selectCity = (city) => App.selectCity(city);
window.filterBrand = (b) => App.filterBrand(b);
window.handleSearch = (q) => App.handleSearch(q);

// Bootstrap on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  App.init();
});
