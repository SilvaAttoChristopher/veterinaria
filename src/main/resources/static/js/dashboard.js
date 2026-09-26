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
