// ===== NUEVO: utilidades de validación =====
const Validator = {
    // Marca un campo en rojo y muestra el mensaje debajo
    markInvalid(input, message) {
        input.classList.add('is-invalid');

        let feedback = input.parentElement.querySelector('.invalid-feedback');
        if (!feedback) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            input.insertAdjacentElement('afterend', feedback);
        }
        feedback.textContent = message;
    },

    // Quita el estado de error de un campo
    clearInvalid(input) {
        input.classList.remove('is-invalid');
        const feedback = input.parentElement.querySelector('.invalid-feedback');
        if (feedback) feedback.remove();
    },

    // Valida una lista de ids. Devuelve true si todos son válidos
    validateFields(ids) {
        let valid = true;
        let firstInvalid = null;

        ids.forEach(id => {
            const input = document.getElementById(id);
            if (!input) return;

            const value = input.value.trim();

            if (!value) {
                this.markInvalid(input, 'Este campo es obligatorio');
                valid = false;
                if (!firstInvalid) firstInvalid = input;
            } else if (input.type === 'email' && !input.checkValidity()) {
                this.markInvalid(input, 'Ingrese un correo válido');
                valid = false;
                if (!firstInvalid) firstInvalid = input;
            } else {
                this.clearInvalid(input);
            }
        });

        if (firstInvalid) firstInvalid.focus();
        return valid;
    },

    // Quita el error automáticamente cuando el usuario empieza a escribir
    attachLiveClear(ids) {
        ids.forEach(id => {
            const input = document.getElementById(id);
            if (input) {
                input.addEventListener('input', () => this.clearInvalid(input));
            }
        });
    },

    // Limpia todos los errores de un contenedor (modal o formulario)
    clearAll(container) {
        if (!container) return;
        container.querySelectorAll('.is-invalid').forEach(input => this.clearInvalid(input));
    }
};

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

document.addEventListener('DOMContentLoaded', function() {

    const params = new URLSearchParams(window.location.search);
    if (params.get('login') === 'true') {
        const loginModalEl = document.getElementById('Login');
        if (loginModalEl && typeof bootstrap !== 'undefined') {
            new bootstrap.Modal(loginModalEl).show();
        }
    }

    // ===== NUEVO: ids de los campos y limpieza en vivo =====
    const loginFields = ['login-email', 'login-password'];
    const registerFields = [
        'reg-nombres', 'reg-apellidos', 'reg-email',
        'reg-password', 'reg-dni', 'reg-telefono', 'reg-direccion'
    ];
    const staffFields = ['staff-email', 'staff-password'];

    Validator.attachLiveClear(loginFields);
    Validator.attachLiveClear(registerFields);
    Validator.attachLiveClear(staffFields);

    // ===== NUEVO: limpiar errores al cerrar los modales =====
    ['Login', 'Registro'].forEach(id => {
        const modalEl = document.getElementById(id);
        if (modalEl) {
            modalEl.addEventListener('hidden.bs.modal', () => Validator.clearAll(modalEl));
        }
    });

    const loginBtn = document.querySelector('#loginBtn');
    if (loginBtn) {
        loginBtn.addEventListener('click', async function() {
            // CAMBIO: validación visual en lugar de alert
            if (!Validator.validateFields(loginFields)) return;

            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;

            const result = await Auth.login(email, password);
            if (result.success) {
                Auth.redirectBasedOnRole(result.data.rol);
            } else {
                alert(result.message);
            }
        });
    }

    const registerBtn = document.querySelector('#registerBtn');
    if (registerBtn) {
        registerBtn.addEventListener('click', async function() {
            // CAMBIO: validación visual en lugar de alert
            if (!Validator.validateFields(registerFields)) return;

            const nombres = document.getElementById('reg-nombres').value;
            const apellidos = document.getElementById('reg-apellidos').value;
            const correo = document.getElementById('reg-email').value;
            const password = document.getElementById('reg-password').value;
            const dni = document.getElementById('reg-dni').value;
            const telefono = document.getElementById('reg-telefono').value;
            const direccion = document.getElementById('reg-direccion').value;

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

    const staffLoginForm = document.getElementById('staffLoginForm');
    if (staffLoginForm) {
        staffLoginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            // NUEVO: validación visual
            if (!Validator.validateFields(staffFields)) return;

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

    document.querySelectorAll('.cerrar-sesion').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            Auth.logout();
        });
    });
});