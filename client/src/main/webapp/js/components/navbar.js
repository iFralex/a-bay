/**
 * Componente Navbar per la navigazione
 */
const Navbar = {
    container: null,
    user: null,
    
    /**
     * Inizializza la navbar
     * @param {string} containerId - ID del contenitore HTML della navbar
     * @param {Object|null} user - Oggetto utente con le informazioni, null se non autenticato
     */
    init: function(containerId, user = null) {
        this.container = document.getElementById(containerId);
        this.user = user;

        if (!this.container) {
            console.error(`Container della navbar con ID "${containerId}" non trovato`);
            return;
        }
        
        this.render();
    },
    
    /**
     * Renderizza la navbar
     */
    render: function() {
        if (!this.container) return;
        console.log("render")
        let html = `
            <div class="navbar-brand">
                <a href="#" data-route="home">a-bay</a>
            </div>
            <div class="navbar-links">
                <a href="#" data-route="acquisto">Cerca Aste</a>
        `;
        
        // Link diversi a seconda se l'utente è autenticato o meno
        if (this.user) {
            html += `
                <a href="#" data-route="gestioneAste">Le Mie Aste</a>
                <a href="/logout">Logout (${this.user.username})</a>
            `;
        } else {
            html += `
                <a href="/login">Login</a>
            `;
        }
        
        html += `</div>`;
        this.container.innerHTML = html;
        
        // Aggiungo event listeners per i link di navigazione
        this.container.querySelectorAll('[data-route]').forEach(link => {
            link.addEventListener('click', (event) => {
                event.preventDefault();
                const route = link.getAttribute('data-route');
                
                // Dispatch di un evento custom per il router
                window.dispatchEvent(new CustomEvent('navigate', {
                    detail: { route: route }
                }));
            });
        });
    },
    
    /**
     * Aggiorna lo stato dell'utente
     * @param {Object|null} user - Oggetto utente aggiornato
     */
    updateUser: function(user) {
        this.user = user;
        this.render();
    }
};