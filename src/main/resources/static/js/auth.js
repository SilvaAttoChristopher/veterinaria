// Auth utility functions
const Auth = {
    async login(username, password) {
        try {
            const formData = new URLSearchParams();
            formData.append('username', username);
            formData.append('password', password);

            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData,
                credentials: 'same-origin'
            });

            const data = await response.json();
            if (response.ok) {
                return { success: true, data };
            } else {
                return { success: false, message: data.mensaje || 'Credenciales incorrectas' };
            }
        } catch (error) {
            console.error('Error during login:', error);
            return { success: false, message: 'Error de conexión' };
        }
    },

    async register(userData) {
        try {
            const response = await fetch('/api/auth/registro', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData),
                credentials: 'same-origin'
            });

            const data = await response.json();
            if (response.ok) {
                return { success: true, data };
            } else {
                return { success: false, message: data.mensaje || 'Error en el registro' };
            }
        } catch (error) {
            console.error('Error during registration:', error);
            return { success: false, message: 'Error de conexión' };
        }
    },

    async getCurrentUser() {
        try {
            const response = await fetch('/api/auth/me', { credentials: 'same-origin' });
            if (response.ok) {
                const data = await response.json();
                return { success: true, data };
            }
            return { success: false, data: null };
        } catch (error) {
            console.error('Error getting current user:', error);
            return { success: false, data: null };
        }
    },

    async logout() {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'same-origin'
            });
        } catch (error) {
            console.error('Error during logout:', error);
        }
        window.location.href = '/';
    },

    async isAuthenticated() {
        const result = await this.getCurrentUser();
        return result.success;
    },

    redirectBasedOnRole(rol) {
        switch (rol) {
            case 'ROLE_CLIENTE':
                window.location.href = '/dashboard/perfil';
                break;
            case 'ROLE_VETERINARIO':
                window.location.href = '/dashboard/perfil-medico';
                break;
            case 'ROLE_ADMIN':
                window.location.href = '/dashboard/admin';
                break;
            default:
                window.location.href = '/';
        }
    }
};

// DOM event handlers
document.addEventListener('DOMContentLoaded', function() {

    // --- LOGIN MODAL ---
    const loginBtn = document.querySelector('#loginBtn');
    if (loginBtn) {
        loginBtn.addEventListener('click', async function() {
            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;

            if (!email || !password) {
                alert('Complete todos los campos');
                return;
            }

            const result = await Auth.login(email, password);
            if (result.success) {
                Auth.redirectBasedOnRole(result.data.rol);
            } else {
                alert(result.message);
            }
        });
    }

    // --- REGISTER MODAL ---
    const registerBtn = document.querySelector('#registerBtn');
    if (registerBtn) {
        registerBtn.addEventListener('click', async function() {
            const nombres = document.getElementById('reg-nombres').value;
            const apellidos = document.getElementById('reg-apellidos').value;
            const correo = document.getElementById('reg-email').value;
            const password = document.getElementById('reg-password').value;
            const dni = document.getElementById('reg-dni').value;
            const telefono = document.getElementById('reg-telefono').value;
            const direccion = document.getElementById('reg-direccion').value;

            if (!nombres || !apellidos || !correo || !password || !dni) {
                alert('Complete todos los campos obligatorios');
                return;
            }

            const userData = {
                username: correo,
                correo: correo,
                password: password,
                nombres: nombres,
                apellidos: apellidos,
                dni: dni,
                telefono: telefono,
                direccion: direccion
            };

            const result = await Auth.register(userData);
            if (result.success) {
                alert('Registro exitoso. Por favor inicie sesión.');
                const registerModal = bootstrap.Modal.getInstance(document.getElementById('Registro'));
                registerModal.hide();
                setTimeout(() => {
                    new bootstrap.Modal(document.getElementById('Login')).show();
                }, 300);
            } else {
                alert(result.message);
            }
        });
    }

    // --- STAFF LOGIN (staff.html) ---
    const staffLoginForm = document.getElementById('staffLoginForm');
    if (staffLoginForm) {
        staffLoginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const email = document.getElementById('staff-email').value;
            const password = document.getElementById('staff-password').value;

            const result = await Auth.login(email, password);
            if (result.success) {
                Auth.redirectBasedOnRole(result.data.rol);
            } else {
                alert(result.message);
            }
        });
    }

    // --- LOGOUT BUTTONS ---
    document.querySelectorAll('.cerrar-sesion').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            Auth.logout();
        });
    });
});
