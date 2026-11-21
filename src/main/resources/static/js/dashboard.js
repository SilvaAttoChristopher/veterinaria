// Dashboard authentication check
document.addEventListener('DOMContentLoaded', async function() {
    // Check if user is authenticated
    const result = await Auth.getCurrentUser();
    
    if (!result.success) {
        // Redirect to login page if not authenticated
        window.location.href = '/index.html';
        return;
    }

    // Update user info in sidebar
    const user = result.data;
    
    // Update sidebar user info
    const userNameElement = document.querySelector('.usuario-sidebar strong');
    const userRoleElement = document.querySelector('.usuario-sidebar span');
    
    if (userNameElement) {
        userNameElement.textContent = `${user.nombres} ${user.apellidos}`;
    }
    
    if (userRoleElement) {
        userRoleElement.textContent = user.rol.replace('ROLE_', '');
    }

    // Update profile page if exists
    const profileNameElement = document.querySelector('.perfil-usuario h3');
    const profileEmailElement = document.querySelector('.perfil-usuario p');
    
    if (profileNameElement) {
        profileNameElement.textContent = `${user.nombres} ${user.apellidos}`;
    }
    
    if (profileEmailElement) {
        profileEmailElement.textContent = user.correo || '';
    }

    // Update data fields if on profile page
    const nombresField = document.querySelector('p:has(+ .dato-perfil span:contains("NOMBRES"))');
    if (nombresField) {
        nombresField.textContent = user.nombres;
    }

    // Store user data in localStorage for easy access
    localStorage.setItem('currentUser', JSON.stringify(user));
});

// Utility function to get current user from localStorage
function getCurrentUser() {
    const userData = localStorage.getItem('currentUser');
    return userData ? JSON.parse(userData) : null;
}

// Utility function to clear user data
function clearUserData() {
    localStorage.removeItem('currentUser');
}

/**
 * Descarga un reporte en CSV.
 *
 * `encabezados` son los títulos de cada columna y `filas` una lista de arreglos
 * con los valores, en el mismo orden. Se separa con ";" para que Excel en
 * español lo abra correctamente, y el BOM inicial evita que las tildes salgan
 * corruptas.
 */
function exportarReporteCSV(nombreArchivo, encabezados, filas) {
    const escapar = valor => {
        const texto = (valor === null || valor === undefined) ? '' : String(valor);
        return /[";\n\r]/.test(texto) ? '"' + texto.replace(/"/g, '""') + '"' : texto;
    };

    const contenido = [encabezados, ...filas]
        .map(fila => fila.map(escapar).join(';'))
        .join('\r\n');

    const blob = new Blob(['\uFEFF' + contenido], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');

    enlace.href = url;
    enlace.download = nombreArchivo;
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
    URL.revokeObjectURL(url);
}
