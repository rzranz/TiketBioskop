/**
 * CineMax TIX - Authentication Component
 * Handles User Login, Admin Backoffice Gateway, and Session State
 */

const AuthComponent = {
  openLoginModal() {
    const modal = document.getElementById('loginModal');
    if (modal) modal.classList.remove('hidden');
  },

  closeLoginModal() {
    const modal = document.getElementById('loginModal');
    if (modal) modal.classList.add('hidden');
  },

  switchLoginRole(role) {
    const btnUser = document.getElementById('roleBtnUser');
    const btnAdmin = document.getElementById('roleBtnAdmin');
    const formUser = document.getElementById('formUserLogin');
    const formAdmin = document.getElementById('formAdminLogin');

    if (!btnUser || !btnAdmin || !formUser || !formAdmin) return;

    if (role === 'user') {
      btnUser.className = 'py-2 rounded-lg font-bold bg-sky-500 text-slate-950 transition';
      btnAdmin.className = 'py-2 rounded-lg font-bold text-slate-400 hover:text-white transition';
      formUser.classList.remove('hidden');
      formAdmin.classList.add('hidden');
    } else {
      btnAdmin.className = 'py-2 rounded-lg font-bold bg-amber-500 text-slate-950 transition';
      btnUser.className = 'py-2 rounded-lg font-bold text-slate-400 hover:text-white transition';
      formAdmin.classList.remove('hidden');
      formUser.classList.add('hidden');
    }
  },

  submitUserLogin() {
    const nameInput = document.getElementById('loginCustName');
    const emailInput = document.getElementById('loginCustEmail');
    const name = nameInput ? nameInput.value.trim() : 'Budi Santoso';
    const email = emailInput ? emailInput.value.trim() : 'budi@example.com';

    AppState.currentUser.name = name;
    AppState.currentUser.email = email;

    const checkoutNameInput = document.getElementById('checkoutCustName');
    if (checkoutNameInput) {
      checkoutNameInput.value = name;
    }

    this.closeLoginModal();
    Utils.showToast(`Selamat datang kembali, ${name}!`, 'success');
  },

  async submitAdminLogin() {
    const passInput = document.getElementById('loginAdminPassword');
    const pass = passInput ? passInput.value.trim() : '';

    if (pass !== 'admin123') {
      Utils.showToast('Password Admin salah! (Gunakan: admin123)', 'error');
      return;
    }

    AppState.isAdmin = true;
    this.closeLoginModal();
    Utils.showToast('Login Administrator Berhasil! Membuka CMS Backoffice...', 'success');
    AdminComponent.openAdminView();
  }
};

window.AuthComponent = AuthComponent;
window.openLoginModal = () => AuthComponent.openLoginModal();
window.closeLoginModal = () => AuthComponent.closeLoginModal();
window.switchLoginRole = (role) => AuthComponent.switchLoginRole(role);
window.submitUserLogin = () => AuthComponent.submitUserLogin();
window.submitAdminLogin = () => AuthComponent.submitAdminLogin();

