/**
 * CineMax TIX - Movie Presentation Component
 * Clean Code: Handles Now Showing, Coming Soon, Movie Details, and Direct Showtime Selection
 */

const MovieComponent = {
  /**
   * Render Now Showing movie catalog cards
   */
  renderNowShowing(moviesToRender = null) {
    const list = moviesToRender || AppState.movies;
    const container = document.getElementById('nowShowingList');
    if (!container) return;

    container.innerHTML = '';

    list.forEach(movie => {
      const card = document.createElement('div');
      card.className = 'group rounded-2xl overflow-hidden bg-slate-900 border border-slate-800 hover:border-sky-500/50 transition-all duration-300 flex flex-col cursor-pointer shadow-lg hover:shadow-sky-500/10 btn-spring';
      card.onclick = () => this.openMovieDetail(movie);

      const rankBadgeHtml = movie.rankBadge 
        ? `<div class="absolute top-2 left-2 w-6 h-6 rounded-full bg-slate-950/80 backdrop-blur border border-amber-400 text-amber-400 font-black text-[11px] flex items-center justify-center">${movie.rankBadge}</div>` 
        : '';

      card.innerHTML = `
        <div class="relative aspect-[2/3] overflow-hidden bg-slate-950">
          <img src="${movie.posterUrl}" onerror="handleImgError(this, '${movie.title}')" alt="${movie.title}" class="w-full h-full object-cover group-hover:scale-105 transition duration-500">
          ${rankBadgeHtml}
          <div class="absolute bottom-2 right-2 px-1.5 py-0.5 rounded bg-slate-950/80 backdrop-blur text-[10px] font-bold text-amber-400 border border-slate-800">
            <i class="fa-solid fa-star text-[9px]"></i> ${movie.imdbRating}
          </div>
        </div>
        <div class="p-2.5">
          <h3 class="font-bold text-xs text-white truncate">${movie.title}</h3>
          <div class="flex items-center justify-between mt-1 text-[10px] text-slate-400">
            <span>${movie.ageRating} • ${movie.durationMinutes}m</span>
            <span class="text-sky-400 font-semibold">${(movie.genre || '').split(',')[0]}</span>
          </div>
        </div>
      `;
      container.appendChild(card);
    });
  },

  /**
   * Render Coming Soon horizontal slider
   */
  renderComingSoon(comingMovies) {
    const container = document.getElementById('comingSoonList');
    if (!container) return;

    container.innerHTML = '';

    comingMovies.forEach(m => {
      const item = document.createElement('div');
      item.className = 'min-w-[130px] rounded-xl overflow-hidden bg-slate-900 border border-slate-800 flex-shrink-0 cursor-pointer btn-spring';
      item.onclick = () => this.openMovieDetail(m);
      item.innerHTML = `
        <img src="${m.posterUrl}" onerror="handleImgError(this, '${m.title}')" alt="${m.title}" class="w-full h-36 object-cover bg-slate-950">
        <div class="p-2">
          <h4 class="font-bold text-[11px] text-white truncate">${m.title}</h4>
          <span class="text-[9px] text-slate-400 block">${m.durationMinutes} min</span>
        </div>
      `;
      container.appendChild(item);
    });
  },

  /**
   * Open Movie Details view with direct Cinema, Studio, and Showtime selection
   */
  async openMovieDetail(movie) {
    AppState.selectedMovie = movie;
    AppState.selectedShowtime = null; // Reset selection

    document.getElementById('detailTitle').innerText = movie.title;
    document.getElementById('detailGenre').innerText = movie.genre;
    document.getElementById('detailAge').innerText = movie.ageRating;
    document.getElementById('detailDuration').innerText = `${movie.durationMinutes} menit`;
    document.getElementById('detailSynopsis').innerText = movie.synopsis;
    document.getElementById('detailImdb').innerText = movie.imdbRating;
    document.getElementById('detailRotten').innerText = movie.rottenScore;
    document.getElementById('detailPoster').src = movie.posterUrl;
    document.getElementById('detailBackdrop').src = movie.backdropUrl || movie.posterUrl;
    document.getElementById('detailBadge').innerText = movie.rankBadge ? `TOP #${movie.rankBadge}` : 'NOW SHOWING';
    document.getElementById('detailMovieCityLabel').innerText = AppState.currentCity;

    // Reset Proceed Button to locked state
    const btn = document.getElementById('btnProceedToSeatFromMovie');
    btn.disabled = true;
    btn.className = 'flex-1 py-3 rounded-xl bg-slate-800 text-slate-500 font-bold text-xs flex items-center justify-center gap-1.5 transition cursor-not-allowed';
    document.getElementById('btnProceedText').innerText = 'Pilih Jam Tayang Terlebih Dahulu';

    // Render Direct Showtimes for this movie
    await this.renderMovieDirectShowtimes(movie.id);

    document.getElementById('movieDetailModal').classList.remove('hidden');
  },

  /**
   * Render Direct Showtimes grouped by Cinema and Studio inside Movie Detail
   */
  async renderMovieDirectShowtimes(movieId) {
    const container = document.getElementById('movieDirectShowtimesContainer');
    if (!container) return;

    container.innerHTML = `<div class="text-xs text-slate-400 animate-pulse py-2">Memuat jadwal bioskop di kota ${AppState.currentCity}...</div>`;

    try {
      const allShowtimes = await Api.showtimes.get({ movieId });
      const cityCinemaIds = AppState.cinemas.map(c => c.id);
      const filteredShowtimes = (allShowtimes || []).filter(st => cityCinemaIds.includes(st.cinemaId));

      if (filteredShowtimes.length === 0) {
        container.innerHTML = `
          <div class="p-4 rounded-xl bg-slate-950 border border-slate-800 text-center text-xs text-slate-400">
            <i class="fa-solid fa-calendar-xmark text-slate-500 text-lg mb-1 block"></i>
            Jadwal belum dibuka di bioskop kota ${AppState.currentCity}. Coba pilih kota lain atau cek jadwal besok.
          </div>
        `;
        return;
      }

      container.innerHTML = '';

      // Group by Cinema
      AppState.cinemas.forEach(cinema => {
        const cinemaShowtimes = filteredShowtimes.filter(st => st.cinemaId === cinema.id);
        if (cinemaShowtimes.length === 0) return;

        const card = document.createElement('div');
        card.className = 'p-3.5 rounded-2xl bg-slate-950 border border-slate-800 space-y-2.5';

        // Group by Studio Type
        const studioGroups = {};
        cinemaShowtimes.forEach(st => {
          if (!studioGroups[st.studioType]) studioGroups[st.studioType] = [];
          studioGroups[st.studioType].push(st);
        });

        let studiosHtml = '';
        for (const type of Object.keys(studioGroups)) {
          const list = studioGroups[type];
          const price = Utils.formatRupiah(list[0].price);

          studiosHtml += `
            <div class="pt-2 border-t border-slate-800/60">
              <div class="flex justify-between items-center text-[11px] mb-1.5">
                <span class="font-bold text-slate-300">${type}</span>
                <span class="font-mono text-slate-400 text-[10px]">${price}</span>
              </div>
              <div class="flex flex-wrap gap-2">
                ${list.map(st => `
                  <button id="timeBtn-${st.id}" onclick="MovieComponent.selectDirectShowtime(${st.id}, '${cinema.name}', '${type}', '${st.startTime}', ${st.price})" class="time-pill-btn px-3 py-1.5 rounded-lg border border-slate-700 bg-slate-900 font-mono text-xs font-bold text-slate-200">
                    ${st.startTime}
                  </button>
                `).join('')}
              </div>
            </div>
          `;
        }

        card.innerHTML = `
          <div class="flex justify-between items-start">
            <div>
              <h5 class="font-bold text-xs text-white">${cinema.name}</h5>
              <p class="text-[10px] text-slate-400">${cinema.address}</p>
            </div>
            <span class="text-[9px] font-bold px-2 py-0.5 rounded bg-slate-800 text-sky-400 border border-slate-700">${cinema.brand}</span>
          </div>
          ${studiosHtml}
        `;
        container.appendChild(card);
      });
    } catch (e) {
      container.innerHTML = '<div class="text-xs text-rose-400">Gagal memuat jadwal tayang.</div>';
    }
  },

  /**
   * Handle time pill button selection
   */
  selectDirectShowtime(showtimeId, cinemaName, studioType, startTime, price) {
    document.querySelectorAll('.time-pill-btn').forEach(btn => btn.classList.remove('time-pill-active'));

    const activeBtn = document.getElementById(`timeBtn-${showtimeId}`);
    if (activeBtn) activeBtn.classList.add('time-pill-active');

    AppState.selectedShowtime = {
      id: showtimeId,
      movieTitle: AppState.selectedMovie.title,
      cinemaName: cinemaName,
      studioType: studioType,
      startTime: startTime,
      price: price
    };

    // Unlock proceed button
    const btn = document.getElementById('btnProceedToSeatFromMovie');
    btn.disabled = false;
    btn.className = 'btn-spring flex-1 py-3 rounded-xl bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-slate-950 font-black text-xs flex items-center justify-center gap-1.5 shadow-lg shadow-sky-500/25 cursor-pointer';
    document.getElementById('btnProceedText').innerText = `Pesan Tiket • ${startTime} (${studioType})`;

    Utils.showToast(`Jadwal ${startTime} di ${cinemaName} dipilih!`, 'info');
  },

  proceedToSeatSelection() {
    if (!AppState.selectedShowtime) return;
    this.closeMovieDetail();
    BookingComponent.openSeatSelection(
      AppState.selectedShowtime.id,
      AppState.selectedShowtime.movieTitle,
      AppState.selectedShowtime.cinemaName,
      AppState.selectedShowtime.studioType,
      AppState.selectedShowtime.startTime
    );
  },

  closeMovieDetail() {
    const modal = document.getElementById('movieDetailModal');
    if (modal) modal.classList.add('hidden');
  },

  watchTrailer() {
    if (AppState.selectedMovie && AppState.selectedMovie.trailerUrl) {
      window.open(AppState.selectedMovie.trailerUrl, '_blank');
    }
  }
};

window.MovieComponent = MovieComponent;
window.renderNowShowing = (movies) => MovieComponent.renderNowShowing(movies);
window.renderComingSoon = (movies) => MovieComponent.renderComingSoon(movies);
window.openMovieDetail = (movie) => MovieComponent.openMovieDetail(movie);
window.selectDirectShowtime = (id, c, s, t, p) => MovieComponent.selectDirectShowtime(id, c, s, t, p);
window.proceedToSeatSelection = () => MovieComponent.proceedToSeatSelection();
window.closeMovieDetail = () => MovieComponent.closeMovieDetail();
window.watchTrailer = () => MovieComponent.watchTrailer();

