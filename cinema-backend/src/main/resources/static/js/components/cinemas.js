/**
 * CineMax TIX - Cinema & Showtime Listing Component
 * Clean Code: Handles Cinema tab browsing, schedule accordion, and brand/keyword filtering
 */

const CinemaComponent = {
  /**
   * Render Cinema list cards inside Tab 2 (Bioskop & Jadwal)
   */
  renderCinemasTab() {
    const container = document.getElementById('generalCinemaList');
    if (!container) return;
    container.innerHTML = '';

    const cityLabel = document.getElementById('cinemaCityFilterLabel');
    if (cityLabel) cityLabel.innerText = AppState.currentCity;

    const brandFilter = AppState.selectedCinemaBrand || 'ALL';
    const query = (AppState.cinemaSearchQuery || '').toLowerCase().trim();

    let list = AppState.cinemas || [];
    if (brandFilter !== 'ALL') {
      list = list.filter(c => (c.brand || '').toUpperCase() === brandFilter);
    }
    if (query) {
      list = list.filter(c =>
        (c.name || '').toLowerCase().includes(query) ||
        (c.address || '').toLowerCase().includes(query)
      );
    }

    if (list.length === 0) {
      container.innerHTML = `
        <div class="p-6 rounded-2xl bg-slate-950/60 border border-slate-800 text-center text-xs text-slate-400">
          <i class="fa-solid fa-store-slash text-slate-600 text-2xl mb-2 block"></i>
          Tidak ada bioskop ditemukan di kota ${AppState.currentCity} dengan filter saat ini.
        </div>
      `;
      return;
    }

    list.forEach(c => {
      const isCgv = (c.brand || '').toUpperCase().includes('CGV');
      const badgeStyle = isCgv
        ? 'bg-rose-500/10 text-rose-400 border-rose-500/30'
        : 'bg-amber-500/10 text-amber-400 border-amber-500/30';

      const item = document.createElement('div');
      item.className = 'cinema-card rounded-2xl bg-slate-900 border border-slate-800 p-4 shadow-lg transition duration-200';
      item.innerHTML = `
        <div class="flex items-start justify-between cursor-pointer" onclick="CinemaComponent.toggleCinemaSchedule(${c.id}, '${c.name.replace(/'/g, "\\'")}')">
          <div class="space-y-1">
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-bold px-2 py-0.5 rounded border ${badgeStyle}">${c.brand}</span>
              <h4 class="font-bold text-sm text-white">${c.name}</h4>
            </div>
            <p class="text-xs text-slate-400 flex items-center gap-1.5">
              <i class="fa-solid fa-location-dot text-slate-500 text-[10px]"></i> ${c.address}
            </p>
            <div class="flex items-center gap-1.5 text-[10px] text-slate-400 pt-0.5">
              <span class="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-medium">Regular 2D</span>
              <span class="px-1.5 py-0.5 rounded bg-slate-800 text-amber-300 font-medium">Gold Class</span>
              <span class="px-1.5 py-0.5 rounded bg-slate-800 text-sky-300 font-medium">Velvet 2D</span>
            </div>
          </div>
          <button class="btn-spring flex items-center gap-1 text-xs font-bold text-sky-400 bg-sky-500/10 px-2.5 py-1.5 rounded-lg border border-sky-500/20">
            <span>Jadwal</span>
            <i id="cinema-chevron-${c.id}" class="fa-solid fa-chevron-down text-[10px] transition duration-200"></i>
          </button>
        </div>
        <div id="cinema-schedule-${c.id}" class="hidden mt-3 pt-3 border-t border-slate-800/80">
          <!-- Live showtimes populated on toggle -->
        </div>
      `;
      container.appendChild(item);
    });

    // Auto-expand first cinema's schedule for immediate rich UX
    if (list.length > 0) {
      this.toggleCinemaSchedule(list[0].id, list[0].name);
    }
  },

  /**
   * Toggle Cinema Schedule accordion and lazily fetch showtimes
   */
  async toggleCinemaSchedule(cinemaId, cinemaName) {
    const schedContainer = document.getElementById(`cinema-schedule-${cinemaId}`);
    const chevron = document.getElementById(`cinema-chevron-${cinemaId}`);
    if (!schedContainer) return;

    if (!schedContainer.classList.contains('hidden')) {
      schedContainer.classList.add('hidden');
      if (chevron) chevron.className = 'fa-solid fa-chevron-down text-[10px] transition duration-200';
      return;
    }

    schedContainer.classList.remove('hidden');
    if (chevron) chevron.className = 'fa-solid fa-chevron-up text-[10px] transition duration-200';

    if (schedContainer.getAttribute('data-loaded') === 'true') return;

    schedContainer.innerHTML = '<div class="text-center py-2.5 text-xs text-slate-400 animate-pulse"><i class="fa-solid fa-circle-notch fa-spin mr-1.5"></i> Memuat jadwal tayang...</div>';

    try {
      const showtimes = await Api.showtimes.get({ cinemaId });
      if (!showtimes || showtimes.length === 0) {
        schedContainer.innerHTML = '<div class="text-center py-2 text-xs text-slate-500">Belum ada jadwal tayang aktif di bioskop ini hari ini.</div>';
        schedContainer.setAttribute('data-loaded', 'true');
        return;
      }

      const movieGroups = {};
      showtimes.forEach(st => {
        if (!movieGroups[st.movieId]) movieGroups[st.movieId] = [];
        movieGroups[st.movieId].push(st);
      });

      let html = '<div class="space-y-3">';
      for (const mIdStr of Object.keys(movieGroups)) {
        const mId = Number(mIdStr);
        const movie = AppState.movies.find(m => m.id === mId) || { title: `Film #${mId}`, posterUrl: '', genre: 'Film Bioskop', ageRating: '13+' };
        const mShowtimes = movieGroups[mId];

        const studioGroups = {};
        mShowtimes.forEach(st => {
          if (!studioGroups[st.studioType]) studioGroups[st.studioType] = [];
          studioGroups[st.studioType].push(st);
        });

        let studiosHtml = '';
        for (const sType of Object.keys(studioGroups)) {
          const list = studioGroups[sType];
          const price = Utils.formatRupiah(list[0].price);
          studiosHtml += `
            <div class="mt-2">
              <div class="flex items-center justify-between text-[11px] mb-1">
                <span class="font-bold text-slate-300">${sType}</span>
                <span class="font-mono text-slate-400 text-[10px]">${price}</span>
              </div>
              <div class="flex flex-wrap gap-2">
                ${list.map(st => `
                  <button onclick="event.stopPropagation(); BookingComponent.openSeatSelection(${st.id}, '${movie.title.replace(/'/g, "\\'")}', '${cinemaName.replace(/'/g, "\\'")}', '${st.studioType}', '${st.startTime}')" class="btn-spring px-3 py-1.5 rounded-lg border border-slate-700 bg-slate-900 hover:border-sky-500 hover:bg-sky-500/10 text-slate-200 font-mono text-xs font-bold transition">
                    ${st.startTime}
                  </button>
                `).join('')}
              </div>
            </div>
          `;
        }

        html += `
          <div class="p-3 rounded-xl bg-slate-950/80 border border-slate-800/80">
            <div class="flex items-center gap-2.5 mb-1">
              <img src="${movie.posterUrl}" onerror="handleImgError(this)" alt="${movie.title}" class="w-10 h-14 rounded-lg object-cover shadow bg-slate-900 flex-shrink-0">
              <div class="min-w-0 flex-1">
                <h5 class="font-bold text-xs text-white truncate">${movie.title}</h5>
                <div class="flex items-center gap-1.5 text-[10px] text-slate-400 mt-0.5">
                  <span class="px-1.5 py-0.2 rounded bg-slate-800 text-slate-300 font-bold">${movie.ageRating || '13+'}</span>
                  <span class="truncate">${movie.genre || ''}</span>
                </div>
              </div>
            </div>
            ${studiosHtml}
          </div>
        `;
      }
      html += '</div>';

      schedContainer.innerHTML = html;
      schedContainer.setAttribute('data-loaded', 'true');
    } catch (e) {
      schedContainer.innerHTML = '<div class="text-center py-2 text-xs text-rose-400">Gagal memuat jadwal tayang.</div>';
    }
  },

  /**
   * Filter Cinemas by Brand (ALL, CGV, XXI)
   */
  filterBrand(brand) {
    AppState.selectedCinemaBrand = brand;
    document.querySelectorAll('.cinema-brand-pill').forEach(b => {
      b.className = 'cinema-brand-pill btn-spring px-3.5 py-1.5 rounded-full text-xs font-bold border border-slate-800 text-slate-400 hover:border-slate-700';
    });

    const activeBtn = document.getElementById(`cinemaPill-${brand}`);
    if (activeBtn) {
      activeBtn.className = 'cinema-brand-pill btn-spring px-3.5 py-1.5 rounded-full text-xs font-bold bg-sky-500 text-slate-950 border border-transparent';
    }

    this.renderCinemasTab();
  },

  /**
   * Filter Cinemas by text search
   */
  filterSearch(query) {
    AppState.cinemaSearchQuery = query;
    this.renderCinemasTab();
  }
};

window.CinemaComponent = CinemaComponent;
window.filterCinemaBrand = (b) => CinemaComponent.filterBrand(b);
window.filterCinemas = (q) => CinemaComponent.filterSearch(q);
window.toggleCinemaSchedule = (id, name) => CinemaComponent.toggleCinemaSchedule(id, name);
