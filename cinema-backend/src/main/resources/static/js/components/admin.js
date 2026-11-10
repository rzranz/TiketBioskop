/**
 * CineMax TIX - Admin Backoffice CMS Component
 * Clean Code: Handles Movie Catalog, Venues, Studios, Showtimes, and Sales Monitor
 */

const AdminComponent = {
  openAdminView() {
    document.getElementById('customerAppView').classList.add('hidden');
    document.getElementById('adminAppView').classList.remove('hidden');
    this.populateAdminForms();
  },

  switchBackToCustomerApp() {
    document.getElementById('adminAppView').classList.add('hidden');
    document.getElementById('customerAppView').classList.remove('hidden');
    Utils.showToast('Kembali ke tampilan pengguna', 'info');
    App.fetchMovies();
    App.fetchCinemas();
  },

  switchAdminTab(tab) {
    document.getElementById('adminTabMovies').classList.add('hidden');
    document.getElementById('adminTabCinemas').classList.add('hidden');
    document.getElementById('adminTabShowtimes').classList.add('hidden');
    document.getElementById('adminTabSales').classList.add('hidden');

    document.querySelectorAll('.admin-tab-btn').forEach(btn => {
      btn.className = 'admin-tab-btn btn-spring px-4 py-2 rounded-xl text-xs font-bold bg-slate-800 text-slate-300 hover:bg-slate-700';
    });

    if (tab === 'movies') {
      document.getElementById('adminTabMovies').classList.remove('hidden');
      document.getElementById('adminTabBtnMovies').className = 'admin-tab-btn btn-spring px-4 py-2 rounded-xl text-xs font-bold bg-sky-500 text-slate-950';
      this.renderAdminMovieTable();
    } else if (tab === 'cinemas') {
      document.getElementById('adminTabCinemas').classList.remove('hidden');
      document.getElementById('adminTabBtnCinemas').className = 'admin-tab-btn btn-spring px-4 py-2 rounded-xl text-xs font-bold bg-sky-500 text-slate-950';
    } else if (tab === 'showtimes') {
      document.getElementById('adminTabShowtimes').classList.remove('hidden');
      document.getElementById('adminTabBtnShowtimes').className = 'admin-tab-btn btn-spring px-4 py-2 rounded-xl text-xs font-bold bg-sky-500 text-slate-950';
    } else if (tab === 'sales') {
      document.getElementById('adminTabSales').classList.remove('hidden');
      document.getElementById('adminTabBtnSales').className = 'admin-tab-btn btn-spring px-4 py-2 rounded-xl text-xs font-bold bg-sky-500 text-slate-950';
      this.renderAdminSales();
    }
  },

  populateAdminForms() {
    this.renderAdminMovieTable();

    // Populate Cinema dropdowns
    const cinemaSelects = [
      document.getElementById('adminStudioCinemaSelect'),
      document.getElementById('adminStCinemaSelect')
    ];
    cinemaSelects.forEach(sel => {
      if (!sel) return;
      sel.innerHTML = '';
      (AppState.cinemas || []).forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.innerText = `${c.name} (${c.city})`;
        sel.appendChild(opt);
      });
    });

    // Populate Movie dropdown for Showtimes
    const movieSel = document.getElementById('adminStMovieSelect');
    if (movieSel) {
      movieSel.innerHTML = '';
      (AppState.movies || []).forEach(m => {
        const opt = document.createElement('option');
        opt.value = m.id;
        opt.innerText = m.title;
        movieSel.appendChild(opt);
      });
    }

    if (AppState.cinemas && AppState.cinemas.length > 0) {
      this.loadStudiosForAdminShowtime(AppState.cinemas[0].id);
    }

    const dateInp = document.getElementById('adminStDate');
    if (dateInp) dateInp.value = new Date().toISOString().split('T')[0];
  },

  async loadStudiosForAdminShowtime(cinemaId) {
    const sel = document.getElementById('adminStStudioSelect');
    if (!sel) return;
    sel.innerHTML = '';
    try {
      const studios = await Api.cinemas.getStudios(cinemaId);
      (studios || []).forEach(st => {
        const opt = document.createElement('option');
        opt.value = st.id;
        opt.innerText = `${st.name} (${st.studioType})`;
        sel.appendChild(opt);
      });
    } catch (e) {
      console.error('Failed to load studios for admin:', e);
    }
  },

  renderAdminMovieTable() {
    const tbody = document.getElementById('adminMovieTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    (AppState.movies || []).forEach(m => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td class="p-3"><img src="${m.posterUrl}" onerror="handleImgError(this)" alt="${m.title}" class="w-8 h-12 rounded object-cover"></td>
        <td class="p-3 font-bold text-white">${m.title}</td>
        <td class="p-3">${m.genre}</td>
        <td class="p-3">${m.durationMinutes} m</td>
        <td class="p-3 text-amber-400 font-bold">${m.imdbRating}</td>
        <td class="p-3"><span class="px-2 py-0.5 rounded text-[10px] font-bold ${m.nowShowing ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-800 text-slate-400'}">${m.nowShowing ? 'Tayang' : 'Coming Soon'}</span></td>
      `;
      tbody.appendChild(tr);
    });
  },

  async submitNewMovie() {
    const title = document.getElementById('adminMovieTitle').value.trim();
    const genre = document.getElementById('adminMovieGenre').value.trim();
    const duration = parseInt(document.getElementById('adminMovieDuration').value);
    const imdb = document.getElementById('adminMovieImdb').value.trim() || '8.0/10';
    const age = document.getElementById('adminMovieAge').value;
    const poster = document.getElementById('adminMoviePoster').value.trim() || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=500&q=80';
    const synopsis = document.getElementById('adminMovieSynopsis').value.trim() || 'Sinopsis film baru.';

    if (!title || !genre || !duration) {
      Utils.showToast('Lengkapi Judul, Genre, dan Durasi Film!', 'error');
      return;
    }

    const payload = {
      title,
      genre,
      durationMinutes: duration,
      imdbRating: imdb,
      rottenScore: '90%',
      ageRating: age,
      posterUrl: poster,
      backdropUrl: poster,
      synopsis,
      trailerUrl: 'https://youtube.com',
      nowShowing: true
    };

    try {
      await Api.movies.create(payload);
      Utils.showToast(`Film '${title}' berhasil ditambahkan ke katalog!`, 'success');
      document.getElementById('adminMovieTitle').value = '';
      await App.fetchMovies();
      this.renderAdminMovieTable();
    } catch (e) {
      Utils.showToast('Gagal menyimpan film: ' + e.message, 'error');
    }
  },

  async submitNewCinema() {
    const name = document.getElementById('adminCinemaName').value.trim();
    const city = document.getElementById('adminCinemaCity').value;
    const brand = document.getElementById('adminCinemaBrand').value;
    const address = document.getElementById('adminCinemaAddress').value.trim();

    if (!name) {
      Utils.showToast('Nama bioskop wajib diisi!', 'error');
      return;
    }

    try {
      await Api.cinemas.create({ name, city, brand, address });
      Utils.showToast(`Bioskop '${name}' berhasil didaftarkan!`, 'success');
      document.getElementById('adminCinemaName').value = '';
      await App.fetchCinemas();
      this.populateAdminForms();
    } catch (e) {
      Utils.showToast('Gagal mendaftarkan bioskop: ' + e.message, 'error');
    }
  },

  async submitNewStudio() {
    const cinemaId = document.getElementById('adminStudioCinemaSelect').value;
    const name = document.getElementById('adminStudioName').value.trim();
    const type = document.getElementById('adminStudioType').value;

    if (!name) {
      Utils.showToast('Nama studio wajib diisi!', 'error');
      return;
    }

    try {
      await Api.cinemas.createStudio(cinemaId, { name, studioType: type, totalSeats: 34 });
      Utils.showToast(`Studio '${name}' berhasil ditambahkan ke bioskop!`, 'success');
      document.getElementById('adminStudioName').value = '';
    } catch (e) {
      Utils.showToast('Gagal menambahkan studio: ' + e.message, 'error');
    }
  },

  async submitNewShowtime() {
    const movieId = document.getElementById('adminStMovieSelect').value;
    const cinemaId = document.getElementById('adminStCinemaSelect').value;
    const studioId = document.getElementById('adminStStudioSelect').value;
    const date = document.getElementById('adminStDate').value;
    const time = document.getElementById('adminStTime').value.trim();
    const price = parseFloat(document.getElementById('adminStPrice').value);

    if (!time || !price) {
      Utils.showToast('Lengkapi Jam Tayang dan Harga Tiket!', 'error');
      return;
    }

    const selStudio = document.getElementById('adminStStudioSelect');
    const studioType = selStudio.selectedOptions[0]?.innerText.split('(')[1]?.replace(')', '') || 'REGULAR 2D';

    try {
      await Api.showtimes.create({
        movieId: parseInt(movieId),
        cinemaId: parseInt(cinemaId),
        studioId: parseInt(studioId),
        studioType: studioType.trim(),
        showDate: date,
        startTime: time,
        price: price
      });
      Utils.showToast(`Jadwal jam ${time} berhasil dibuat beserta 34 kursi siap dipesan!`, 'success');
      document.getElementById('adminStTime').value = '';
    } catch (e) {
      Utils.showToast('Gagal membuat jadwal tayang: ' + e.message, 'error');
    }
  },

  renderAdminSales() {
    const container = document.getElementById('adminSalesContainer');
    if (!container) return;
    container.innerHTML = '';

    let totalRevenue = 0;
    (AppState.activeBookings || []).forEach(b => totalRevenue += (b.grandTotal || 0));

    const stats = document.createElement('div');
    stats.className = 'grid grid-cols-1 sm:grid-cols-3 gap-3';
    stats.innerHTML = `
      <div class="p-4 rounded-xl bg-slate-950 border border-slate-800">
        <div class="text-[10px] text-slate-400 uppercase font-bold">Total Transaksi</div>
        <div class="text-xl font-black text-white mt-1">${(AppState.activeBookings || []).length} Tiket</div>
      </div>
      <div class="p-4 rounded-xl bg-slate-950 border border-slate-800">
        <div class="text-[10px] text-slate-400 uppercase font-bold">Total Pendapatan</div>
        <div class="text-xl font-black text-amber-400 mt-1">${Utils.formatRupiah(totalRevenue)}</div>
      </div>
      <div class="p-4 rounded-xl bg-slate-950 border border-slate-800">
        <div class="text-[10px] text-slate-400 uppercase font-bold">Kapasitas Kursi Terisi</div>
        <div class="text-xl font-black text-sky-400 mt-1">94% Okupansi</div>
      </div>
    `;
    container.appendChild(stats);

    const tableDiv = document.createElement('div');
    tableDiv.className = 'overflow-x-auto rounded-2xl border border-slate-800 mt-4';
    tableDiv.innerHTML = `
      <table class="w-full text-left text-xs text-slate-300">
        <thead class="bg-slate-950 text-slate-400 border-b border-slate-800">
          <tr>
            <th class="p-3">Kode Booking</th>
            <th class="p-3">PIN Klaim</th>
            <th class="p-3">Pelanggan</th>
            <th class="p-3">Film &amp; Studio</th>
            <th class="p-3">Kursi</th>
            <th class="p-3">Total Bayar</th>
            <th class="p-3">Cetak Kiosk</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-800 bg-slate-900">
          ${(AppState.activeBookings || []).map(b => `
            <tr>
              <td class="p-3 font-mono text-sky-400">${b.bookingCode}</td>
              <td class="p-3 font-mono font-bold text-amber-400">${b.claimCode}</td>
              <td class="p-3 text-white font-medium">${b.customerName}</td>
              <td class="p-3">${b.movieTitle} (${b.studioName})</td>
              <td class="p-3 font-mono">${b.seatCodesSummary}</td>
              <td class="p-3 font-mono text-slate-200">${Utils.formatRupiah(b.grandTotal)}</td>
              <td class="p-3"><span class="px-2 py-0.5 rounded text-[10px] font-bold ${b.ticketClaimed ? 'bg-slate-800 text-slate-400' : 'bg-emerald-500/20 text-emerald-400'}">${b.ticketClaimed ? 'TERCETAK' : 'BELUM'}</span></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
    container.appendChild(tableDiv);
  }
};

window.AdminComponent = AdminComponent;
window.openAdminView = () => AdminComponent.openAdminView();
window.switchBackToCustomerApp = () => AdminComponent.switchBackToCustomerApp();
window.switchAdminTab = (tab) => AdminComponent.switchAdminTab(tab);
window.populateAdminForms = () => AdminComponent.populateAdminForms();
window.loadStudiosForAdminShowtime = (id) => AdminComponent.loadStudiosForAdminShowtime(id);
window.renderAdminMovieTable = () => AdminComponent.renderAdminMovieTable();
window.submitNewMovie = () => AdminComponent.submitNewMovie();
window.submitNewCinema = () => AdminComponent.submitNewCinema();
window.submitNewStudio = () => AdminComponent.submitNewStudio();
window.submitNewShowtime = () => AdminComponent.submitNewShowtime();
window.renderAdminSales = () => AdminComponent.renderAdminSales();

