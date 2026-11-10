/**
 * CineMax TIX - Tickets & Self-Service Kiosk Component
 * Clean Code: Handles E-Ticket Display, Barcode generation, and Venue Kiosk Printing Simulator
 */

const TicketsComponent = {
  /**
   * Display modal containing digital ticket barcode, PIN, and show details
   */
  showTicketDetail(ticket) {
    AppState.viewingTicket = ticket;
    document.getElementById('ticketMovie').innerText = ticket.movieTitle;
    document.getElementById('ticketCinema').innerText = `${ticket.cinemaName} • ${ticket.studioName}`;
    document.getElementById('ticketTime').innerText = ticket.showDateTime;
    document.getElementById('ticketSeats').innerText = ticket.seatCodesSummary;
    document.getElementById('ticketFnb').innerText = ticket.foodSummary;
    document.getElementById('ticketClaimCode').innerText = ticket.claimCode;
    document.getElementById('ticketBarcodeText').innerText = ticket.bookingCode;
    document.getElementById('ticketDetailModal').classList.remove('hidden');
  },

  closeTicketDetailModal() {
    const modal = document.getElementById('ticketDetailModal');
    if (modal) modal.classList.add('hidden');
  },

  /**
   * Render list of active bookings in the "Tiket Saya" tab
   */
  renderActiveTickets() {
    const container = document.getElementById('activeTicketList');
    if (!container) return;

    container.innerHTML = '';

    if (!AppState.activeBookings || AppState.activeBookings.length === 0) {
      container.innerHTML = `
        <div class="text-center py-12 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-slate-800 flex items-center justify-center text-slate-500 text-2xl mb-3">
            <i class="fa-solid fa-ticket-simple"></i>
          </div>
          <h4 class="font-bold text-sm text-slate-200">Belum Ada Tiket Aktif</h4>
          <p class="text-xs text-slate-400 mt-1 max-w-xs">Pesan tiket film favorit Anda sekarang untuk menikmati serunya menonton di bioskop!</p>
          <button onclick="App.switchTab('home')" class="btn-spring mt-4 px-5 py-2 rounded-xl bg-sky-500 text-slate-950 font-bold text-xs">Lihat Film</button>
        </div>
      `;
      return;
    }

    AppState.activeBookings.forEach(t => {
      const card = document.createElement('div');
      card.className = 'btn-spring p-4 rounded-2xl bg-slate-950 border border-slate-800 space-y-3 cursor-pointer hover:border-sky-500/50 shadow';
      card.onclick = () => this.showTicketDetail(t);

      const claimStatusHtml = t.ticketClaimed
        ? '<span class="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-slate-400">TIKET SUDAH DICETAK</span>'
        : '<span class="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400">SIAP DITUKAR DI KIOSK</span>';

      const posterUrl = t.moviePosterUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=150&q=80';

      card.innerHTML = `
        <div class="flex items-center justify-between border-b border-slate-800/80 pb-2.5">
          <span class="text-[10px] font-mono text-slate-400">${t.bookingCode}</span>
          ${claimStatusHtml}
        </div>
        <div class="flex gap-3">
          <img src="${posterUrl}" onerror="handleImgError(this, '${t.movieTitle}')" class="w-14 h-20 rounded-xl object-cover bg-slate-900">
          <div class="flex-1">
            <h4 class="font-bold text-xs text-white">${t.movieTitle}</h4>
            <p class="text-[11px] text-slate-400">${t.cinemaName}</p>
            <p class="text-[10px] text-sky-400 mt-1 font-semibold">${t.showDateTime}</p>
            <div class="flex justify-between items-center mt-2">
              <span class="text-xs font-mono font-bold text-amber-400">Kursi: ${t.seatCodesSummary}</span>
              <span class="text-[10px] bg-slate-800 px-2 py-0.5 rounded font-mono font-bold text-slate-300">PIN: ${t.claimCode}</span>
            </div>
          </div>
        </div>
      `;
      container.appendChild(card);
    });
  },

  /**
   * Kiosk Simulator: Open Self-Ticketing Kiosk Modal
   */
  openKioskModal() {
    document.getElementById('kioskPrintResult').classList.add('hidden');
    document.getElementById('kioskModal').classList.remove('hidden');
  },

  openKioskWithCode() {
    this.closeTicketDetailModal();
    if (AppState.viewingTicket) {
      document.getElementById('kioskCodeInput').value = AppState.viewingTicket.claimCode;
    }
    this.openKioskModal();
  },

  closeKioskModal() {
    const modal = document.getElementById('kioskModal');
    if (modal) modal.classList.add('hidden');
  },

  /**
   * Kiosk Simulator: Submit Claim PIN and trigger thermal paper print animation
   */
  async submitKioskClaim() {
    const code = document.getElementById('kioskCodeInput').value.trim();
    if (!code) {
      Utils.showToast('Masukkan PIN / Kode Klaim!', 'error');
      return;
    }

    try {
      const claimResult = await Api.bookings.claimTicket(null, code);
      document.getElementById('kioskPrintMovie').innerText = (claimResult.movieTitle || '').toUpperCase();
      document.getElementById('kioskPrintStudio').innerText = (claimResult.studioName || '').toUpperCase();
      document.getElementById('kioskPrintSeat').innerText = claimResult.seatCodesSummary;
      document.getElementById('kioskPrintTime').innerText = claimResult.showDateTime;
      document.getElementById('kioskPrintResult').classList.remove('hidden');

      Utils.showToast('Tiket fisik berhasil dicetak di Kiosk!', 'success');
      App.fetchActiveBookings();
    } catch (e) {
      Utils.showToast('Gagal: ' + e.message, 'error');
    }
  }
};

window.TicketsComponent = TicketsComponent;
window.showTicketDetail = (t) => TicketsComponent.showTicketDetail(t);
window.closeTicketDetailModal = () => TicketsComponent.closeTicketDetailModal();
window.renderActiveTickets = () => TicketsComponent.renderActiveTickets();
window.openKioskModal = () => TicketsComponent.openKioskModal();
window.openKioskWithCode = () => TicketsComponent.openKioskWithCode();
window.closeKioskModal = () => TicketsComponent.closeKioskModal();
window.submitKioskClaim = () => TicketsComponent.submitKioskClaim();

