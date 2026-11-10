/**
 * CineMax TIX - Booking & Seat Selection Component
 * Clean Code: Handles Seat Matrix, Concurrency TTL Hold, Concessions, and Checkout Payment
 */

const BookingComponent = {
  selectedSeats: new Map(), // seatCode -> price
  selectedFnb: new Map(),   // foodId -> qty
  holdTimerInterval: null,

  /**
   * Open Interactive Seat Selection View
   */
  async openSeatSelection(showtimeId, movieTitle, cinemaName, studioType, startTime) {
    AppState.selectedShowtime = { id: showtimeId, movieTitle, cinemaName, studioType, startTime };
    this.selectedSeats.clear();

    document.getElementById('seatModalMovieTitle').innerText = movieTitle;
    document.getElementById('seatModalCinemaInfo').innerText = `${cinemaName} • ${studioType} • ${startTime}`;
    document.getElementById('selectedSeatsCodes').innerText = '-';
    document.getElementById('selectedSeatsTotal').innerText = Utils.formatRupiah(0);

    await this.loadShowtimeSeats(showtimeId);
    document.getElementById('seatModal').classList.remove('hidden');
  },

  /**
   * Fetch and render seat matrix with status colors (Booked: Red, Held: Grey, Available: Tier Colors)
   */
  async loadShowtimeSeats(showtimeId) {
    const container = document.getElementById('showtimeSeatMap');
    if (!container) return;

    container.innerHTML = '<div class="text-xs text-slate-400 animate-pulse py-4">Memuat denah kursi...</div>';

    try {
      const seats = await Api.seats.getSeats(showtimeId);
      container.innerHTML = '';

      const rows = {};
      (seats || []).forEach(s => {
        if (!rows[s.seatRow]) rows[s.seatRow] = [];
        rows[s.seatRow].push(s);
      });

      Object.keys(rows).sort().forEach(rowKey => {
        const rowDiv = document.createElement('div');
        rowDiv.className = 'flex items-center gap-2';

        const rowLabel = document.createElement('div');
        rowLabel.className = 'w-5 text-center font-bold text-xs text-slate-500';
        rowLabel.innerText = rowKey;
        rowDiv.appendChild(rowLabel);

        const seatsContainer = document.createElement('div');
        seatsContainer.className = 'flex gap-1.5';

        rows[rowKey].forEach(seat => {
          const btn = document.createElement('button');
          const isSelected = this.selectedSeats.has(seat.seatCode);

          // Seat Status Logic: BOOKED, HELD (Abu-abu), or AVAILABLE
          if (seat.status === 'BOOKED') {
            btn.className = 'w-7 h-7 rounded-md bg-red-950/60 border border-red-800/40 text-red-500 opacity-50 cursor-not-allowed text-[10px] flex items-center justify-center';
            btn.innerHTML = '<i class="fa-solid fa-xmark"></i>';
            btn.disabled = true;
          } else if (seat.status === 'HELD') {
            btn.className = 'w-7 h-7 rounded-md bg-slate-700/80 border border-slate-600 text-slate-400 cursor-not-allowed text-[10px] flex items-center justify-center shadow';
            btn.innerHTML = '<i class="fa-solid fa-lock text-[8px]"></i>';
            btn.title = 'Sedang dalam proses pembayaran oleh pembeli lain';
            btn.disabled = true;
          } else if (isSelected) {
            btn.className = 'w-7 h-7 rounded-md bg-amber-500 text-slate-950 font-bold text-[10px] flex items-center justify-center shadow-md shadow-amber-500/30 scale-110 transition-transform';
            btn.innerText = seat.seatCode;
            btn.onclick = () => this.toggleShowtimeSeat(seat);
          } else {
            let tierColor = 'border-sky-500/40 bg-sky-950/40 text-sky-300 hover:border-sky-400';
            if (seat.tier === 'SATINE_SUITE') tierColor = 'border-purple-500/40 bg-purple-950/40 text-purple-300';
            if (seat.tier === 'SWEET_BOX') tierColor = 'border-pink-500/40 bg-pink-950/40 text-pink-300';

            btn.className = `btn-spring w-7 h-7 rounded-md border ${tierColor} text-[10px] font-bold flex items-center justify-center`;
            btn.innerText = seat.seatCode;
            btn.onclick = () => this.toggleShowtimeSeat(seat);
          }

          seatsContainer.appendChild(btn);
        });

        rowDiv.appendChild(seatsContainer);
        container.appendChild(rowDiv);
      });
    } catch (e) {
      container.innerHTML = '<div class="text-xs text-rose-400">Gagal memuat denah kursi.</div>';
    }
  },

  /**
   * Toggle seat selection state
   */
  toggleShowtimeSeat(seat) {
    if (this.selectedSeats.has(seat.seatCode)) {
      this.selectedSeats.delete(seat.seatCode);
    } else {
      this.selectedSeats.set(seat.seatCode, seat.price);
    }

    const codes = Array.from(this.selectedSeats.keys());
    document.getElementById('selectedSeatsCodes').innerText = codes.length > 0 ? codes.join(', ') : '-';

    let total = 0;
    this.selectedSeats.forEach(p => total += p);
    document.getElementById('selectedSeatsTotal').innerText = Utils.formatRupiah(total);

    this.loadShowtimeSeats(AppState.selectedShowtime.id);
  },

  closeSeatModal() {
    const modal = document.getElementById('seatModal');
    if (modal) modal.classList.add('hidden');
  },

  /**
   * Hold seats (turns status to HELD/grey across all users) and open checkout dialog
   */
  async proceedToCheckoutWithHold() {
    if (this.selectedSeats.size === 0) {
      Utils.showToast('Pilih minimal 1 kursi terlebih dahulu!', 'error');
      return;
    }

    const seatCodes = Array.from(this.selectedSeats.keys());
    const custName = document.getElementById('checkoutCustName').value.trim() || AppState.currentUser.name;

    try {
      await Api.seats.hold(AppState.selectedShowtime.id, seatCodes, custName);
      Utils.showToast('Kursi berhasil dikunci (Status Abu-abu)!', 'success');
      this.closeSeatModal();
      this.openCheckoutModal();
    } catch (e) {
      Utils.showToast('Gagal mengunci kursi: ' + e.message, 'error');
    }
  },

  /**
   * Open checkout modal & start 5-minute countdown timer
   */
  openCheckoutModal() {
    document.getElementById('checkoutMovieTitle').innerText = AppState.selectedShowtime.movieTitle;
    document.getElementById('checkoutStudio').innerText = AppState.selectedShowtime.studioType;
    document.getElementById('checkoutCinemaName').innerText = AppState.selectedShowtime.cinemaName;
    document.getElementById('checkoutSeats').innerText = Array.from(this.selectedSeats.keys()).join(', ');

    let ticketTotal = 0;
    this.selectedSeats.forEach(p => ticketTotal += p);
    document.getElementById('checkoutTicketAmount').innerText = Utils.formatRupiah(ticketTotal);

    this.renderCheckoutFnb();
    this.updateCheckoutGrandTotal();
    this.startHoldTimer(300); // 5 minutes TTL

    document.getElementById('checkoutModal').classList.remove('hidden');
  },

  renderCheckoutFnb() {
    const container = document.getElementById('checkoutFnbContainer');
    if (!container) return;

    container.innerHTML = '';
    (AppState.foodItems || []).slice(0, 4).forEach(item => {
      const qty = this.selectedFnb.get(item.id) || 0;
      const row = document.createElement('div');
      row.className = 'flex items-center justify-between p-2 rounded-xl bg-slate-950 border border-slate-800 text-xs';
      row.innerHTML = `
        <div class="flex items-center gap-2">
          <img src="${item.imageUrl}" onerror="handleImgError(this)" alt="${item.name}" class="w-8 h-8 rounded-lg object-cover bg-slate-900">
          <div>
            <div class="font-bold text-slate-200">${item.name}</div>
            <div class="text-[10px] text-amber-400 font-mono">${Utils.formatRupiah(item.price)}</div>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button onclick="BookingComponent.updateFnbQty(${item.id}, -1)" class="btn-spring w-6 h-6 rounded bg-slate-800 text-slate-300 font-bold">-</button>
          <span class="w-4 text-center font-bold font-mono">${qty}</span>
          <button onclick="BookingComponent.updateFnbQty(${item.id}, 1)" class="btn-spring w-6 h-6 rounded bg-sky-600 text-white font-bold">+</button>
        </div>
      `;
      container.appendChild(row);
    });
  },

  updateFnbQty(foodId, delta) {
    const current = this.selectedFnb.get(foodId) || 0;
    const next = Math.max(0, current + delta);
    if (next === 0) this.selectedFnb.delete(foodId);
    else this.selectedFnb.set(foodId, next);

    this.renderCheckoutFnb();
    this.updateCheckoutGrandTotal();
  },

  updateCheckoutGrandTotal() {
    let ticketSum = 0;
    this.selectedSeats.forEach(p => ticketSum += p);

    let fnbSum = 0;
    this.selectedFnb.forEach((qty, id) => {
      const f = AppState.foodItems.find(x => x.id === id);
      if (f) fnbSum += f.price * qty;
    });

    document.getElementById('fnbTotalLabel').innerText = '+ ' + Utils.formatRupiah(fnbSum);
    const grandTotal = ticketSum + fnbSum;
    document.getElementById('checkoutGrandTotal').innerText = Utils.formatRupiah(grandTotal);

    const checkedPay = document.querySelector('input[name="checkoutPayMethod"]:checked');
    const payMethod = checkedPay ? checkedPay.value : 'QRIS';
    if (payMethod === 'QRIS') {
      document.getElementById('checkoutPayAmountInput').value = grandTotal;
    }
  },

  toggleCheckoutPaymentInput() {
    const checkedPay = document.querySelector('input[name="checkoutPayMethod"]:checked');
    const method = checkedPay ? checkedPay.value : 'QRIS';
    const input = document.getElementById('checkoutPayAmountInput');
    const label = document.getElementById('checkoutPayAmountLabel');

    if (method === 'QRIS') {
      input.readOnly = true;
      label.innerText = 'Nominal QRIS (Otomatis Pas)';
      this.updateCheckoutGrandTotal();
    } else {
      input.readOnly = false;
      input.value = '';
      label.innerText = 'Uang Dibayarkan (Cash)';
    }
  },

  startHoldTimer(seconds) {
    if (this.holdTimerInterval) clearInterval(this.holdTimerInterval);
    let left = seconds;

    this.holdTimerInterval = setInterval(() => {
      left--;
      const m = String(Math.floor(left / 60)).padStart(2, '0');
      const s = String(left % 60).padStart(2, '0');
      const timerEl = document.getElementById('holdTimer');
      if (timerEl) timerEl.innerText = `${m}:${s}`;

      if (left <= 0) {
        clearInterval(this.holdTimerInterval);
        Utils.showToast('Batas waktu habis! Kursi telah dibebaskan kembali.', 'error');
        this.closeCheckoutModal();
      }
    }, 1000);
  },

  closeCheckoutModal() {
    if (this.holdTimerInterval) clearInterval(this.holdTimerInterval);
    const modal = document.getElementById('checkoutModal');
    if (modal) modal.classList.add('hidden');
  },

  /**
   * Finalize transaction and generate digital ticket
   */
  async executePayment() {
    const custName = document.getElementById('checkoutCustName').value.trim() || AppState.currentUser.name;
    const payAmount = parseFloat(document.getElementById('checkoutPayAmountInput').value);
    const checkedPay = document.querySelector('input[name="checkoutPayMethod"]:checked');
    const payMethod = checkedPay ? checkedPay.value : 'QRIS';

    if (!custName) {
      Utils.showToast('Nama pembeli wajib diisi!', 'error');
      return;
    }
    if (!payAmount || isNaN(payAmount)) {
      Utils.showToast('Masukkan nominal uang pembayaran yang valid!', 'error');
      return;
    }

    const foodOrdersObj = {};
    this.selectedFnb.forEach((qty, id) => foodOrdersObj[id] = qty);

    const payload = {
      showtimeId: AppState.selectedShowtime.id,
      customerName: custName,
      customerEmail: AppState.currentUser.email || 'customer@example.com',
      seatCodes: Array.from(this.selectedSeats.keys()),
      foodOrders: foodOrdersObj,
      paymentMethod: payMethod,
      paymentAmount: payAmount
    };

    try {
      const bookingData = await Api.bookings.create(payload);
      this.closeCheckoutModal();
      AppState.viewingTicket = bookingData;
      TicketsComponent.showTicketDetail(bookingData);
      Utils.showToast('Pembayaran berhasil! E-Ticket telah terbit.', 'success');
      App.fetchActiveBookings();
      this.selectedSeats.clear();
      this.selectedFnb.clear();
    } catch (e) {
      Utils.showToast('Gagal: ' + e.message, 'error');
    }
  }
};

window.BookingComponent = BookingComponent;
window.openSeatSelection = (id, m, c, s, t) => BookingComponent.openSeatSelection(id, m, c, s, t);
window.loadShowtimeSeats = (id) => BookingComponent.loadShowtimeSeats(id);
window.toggleShowtimeSeat = (s) => BookingComponent.toggleShowtimeSeat(s);
window.closeSeatModal = () => BookingComponent.closeSeatModal();
window.proceedToCheckoutWithHold = () => BookingComponent.proceedToCheckoutWithHold();
window.openCheckoutModal = () => BookingComponent.openCheckoutModal();
window.renderCheckoutFnb = () => BookingComponent.renderCheckoutFnb();
window.updateFnbQty = (id, delta) => BookingComponent.updateFnbQty(id, delta);
window.updateCheckoutGrandTotal = () => BookingComponent.updateCheckoutGrandTotal();
window.toggleCheckoutPaymentInput = () => BookingComponent.toggleCheckoutPaymentInput();
window.startHoldTimer = (sec) => BookingComponent.startHoldTimer(sec);
window.closeCheckoutModal = () => BookingComponent.closeCheckoutModal();
window.executePayment = () => BookingComponent.executePayment();

