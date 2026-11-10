/**
 * CineMax TIX - Utility Functions & UI Helpers
 * Clean Code: Single Responsibility (Formatting, Toast Notifications, Fallbacks)
 */

const Utils = {
  /**
   * Format number to standard Indonesian Rupiah currency
   * @param {number|string} amount 
   * @returns {string} e.g. "Rp45.000"
   */
  formatRupiah(amount) {
    const val = Number(amount) || 0;
    return 'Rp' + new Intl.NumberFormat('id-ID').format(val);
  },

  /**
   * Floating Toast Notification (Non-blocking modern micro-interaction)
   * @param {string} message 
   * @param {'success'|'error'|'info'|'warning'} type 
   */
  showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    const bgColors = {
      success: 'bg-emerald-600/95 text-white border-emerald-400/30',
      error: 'bg-rose-600/95 text-white border-rose-400/30',
      info: 'bg-sky-600/95 text-white border-sky-400/30',
      warning: 'bg-amber-600/95 text-white border-amber-400/30'
    };
    const icons = {
      success: 'fa-circle-check',
      error: 'fa-triangle-exclamation',
      info: 'fa-circle-info',
      warning: 'fa-bell'
    };

    const colorClass = bgColors[type] || bgColors.success;
    const iconClass = icons[type] || icons.success;

    toast.className = `toast-anim flex items-center gap-2.5 px-4 py-2.5 rounded-2xl shadow-2xl backdrop-blur-md border text-xs font-semibold pointer-events-auto ${colorClass}`;
    toast.innerHTML = `
      <i class="fa-solid ${iconClass} text-sm"></i>
      <span>${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.style.transition = 'all 0.3s cubic-bezier(0.16, 1, 0.3, 1)';
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(-15px)';
      setTimeout(() => toast.remove(), 300);
    }, 3200);
  },

  /**
   * High-contrast SVG Poster Fallback generator to completely prevent broken image icons
   * @param {HTMLImageElement} img 
   */
  handleImgError(img) {
    if (!img || img.dataset.hasFailed) return;
    img.dataset.hasFailed = "true";
    img.onerror = null; // Prevent infinite loop
    const label = img.alt || 'CineMax Film';
    const title = encodeURIComponent(label);
    img.src = `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="450" viewBox="0 0 300 450"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%230f172a"/><stop offset="100%" stop-color="%230284c7"/></linearGradient></defs><rect width="300" height="450" fill="url(%23g)"/><circle cx="150" cy="180" r="48" fill="%2338bdf8" opacity="0.2"/><path d="M135 155 L175 180 L135 205 Z" fill="%2338bdf8"/><text x="150" y="270" fill="%23f8fafc" font-size="16" font-weight="bold" font-family="sans-serif" text-anchor="middle">${title}</text><text x="150" y="295" fill="%2394a3b8" font-size="12" font-family="sans-serif" text-anchor="middle">CineMax Premiere</text></svg>`;
  }
};

// Global export
window.Utils = Utils;
window.showToast = Utils.showToast;
window.formatRupiah = Utils.formatRupiah;
window.handleImgError = Utils.handleImgError;

