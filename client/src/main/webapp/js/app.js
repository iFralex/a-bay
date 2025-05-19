/**
 * File principale dell'applicazione
 * Gestisce il routing e l'inizializzazione dei componenti
 */
const App = {
    container: null,
    messagesContainer: null,
    navbarContainer: null,
    currentUser: null, // To store the logged-in user data
    routes: {
        'gestioneAste': GestioneAstePage,
        'dettaglioAsta': DettaglioAstaPage,
        'ricercaAste': RicercaAstePage,
        'offerta': OffertaPage,
        // Add other routes here as pages are implemented (e.g., 'login', 'register', 'profilo')
    },
    defaultRoute: 'ricercaAste', // Default page to load

    /**
     * Inizializza l'applicazione
     */
    init: async function () {
        console.log("App initializing...");

        // Get main container elements
        this.container = document.getElementById('app-container');
        this.messagesContainer = document.getElementById('messages-container');
        this.navbarContainer = document.getElementById('navbar',);

        if (!this.container || !this.messagesContainer || !this.navbarContainer) {
            console.error("Critical: Missing main application containers in HTML.");
            return; // Cannot proceed without containers
        }

        // Initialize Message Manager
        MessageManager.init('messages-container');
        console.log("MessageManager initialized.");

        // --- User Authentication (
        await this.checkAuthentication(); // Assume this function fetches user data if logged in
        console.log("Authentication check completed. Current user:", this.currentUser);
        // --- End User Authentication ---


        // Initialize Navbar (might depend on currentUser)
        console.log("a", this.currentUser)
        Navbar.init('navbar', this.currentUser);
        console.log("Navbar initialized and rendered.");

        // Set up navigation event listener
        window.addEventListener('navigate', (event) => {
            const { route, params } = event.detail;
            console.log(`Navigating to route: ${route} with params:`, params);
            this.navigateTo(route, params);
        });
        console.log("Navigation event listener added.");

        // Handle initial page load
        const initialRoute = this.getInitialRoute();
        this.navigateTo(initialRoute.route, initialRoute.params);
        console.log(`Initial route: ${initialRoute.route}`);
        console.log("App initializing...");
    },

    /**
     * Placeholder function to check authentication and set current user
     * In a real app, this would call an API to validate session/token.
     */
    checkAuthentication: async function () {
        try {
            // Example: API call to check login status and get user data
            const { userData } = await API.get('/auth/status'); // Assume this returns user object or null/error
            if (userData && userData.username) {
                this.currentUser = userData; // Store the logged-in user data
                console.log("User is logged in:", this.currentUser.username);
            } else {
                this.currentUser = null;
                console.log("User is not logged in.");
            }
        } catch (error) {
            console.warn("Authentication check failed (API error or not logged in):", error);
            this.currentUser = null; // Assume not logged in on error
        }

        // --- Dummy user for testing if API call is not implemented yet ---
        // Remove this section in a real app
        /*
        this.currentUser = {
            username: 'testuser',
            nome: 'Test',
            cognome: 'User',
            // Add other user properties needed by pages (e.g., indirizzo)
             indirizzo: 'Via Prova, 123',
             // Add venditore flag if needed, or derive from username check
             isVenditore: false // Set to true if you want to test vendor view
        };
        console.log("Using DUMMY user:", this.currentUser.username);
        */
        // --- End Dummy User ---
    },

    /**
     * Returns the current logged-in user object.
     * Pages use this to determine user context (e.g., is vendor, can bid).
     */
    getCurrentUser: function () {
        return this.currentUser;
    },

    /**
     * Determines the initial route based on the URL or defaults.
     * @returns {{route: string, params: object}} - The initial route and parameters.
     */
    getInitialRoute: function () {
        // Simple routing based on hash fragment (e.g., #ricercaAste?id=123)
        const hash = window.location.hash.substring(1); // Remove '#'
        if (!hash) {
            let defaultRoute = this.defaultRoute;
            const primaVisita = Cookies.getCookie("primaVisita");
            if (!primaVisita) {
                // Prima visita: salva cookie e mostra pagina ACQUISTO
                Cookies.setCookie("primaVisita", "true");
                defaultRoute = "ricercaAste";
            }

            const ultimaAzione = Cookies.getCookie("ultimaAzione");
            if (ultimaAzione === "creazione-asta") {
                defaultRoute = "gestioneAste";
            }

            return { route: defaultRoute, params: {} };
        }

        const [route, queryString] = hash.split('?');
        const params = {};
        if (queryString) {
            const pairs = queryString.split('&');
            pairs.forEach(pair => {
                const [key, value] = pair.split('=');
                if (key && value) {
                    params[decodeURIComponent(key)] = decodeURIComponent(value);
                }
            });
        }

        if (this.routes[route]) {
            return { route: route, params: params };
        } else {
            console.warn(`Route "${route}" not found. Navigating to default.`);
            return { route: this.defaultRoute, params: {} };
        }
    },

    /**
     * Navigates to a specific page/route.
     * @param {string} route - The route name (key in App.routes).
     * @param {object} params - Parameters to pass to the page's render method.
     */
    navigateTo: function (route, params = {}) {
        const page = this.routes[route];

        if (page) {
            // Optional: Clear main container before rendering the new page
            // This is handled within each page's render now, but can be done here too.
            // this.container.innerHTML = '';

            // Clear messages from previous page view
            MessageManager.clearMessages();

            Cookies.setCookie("ultimaAzione", page);
            // Initialize the page with its container
            page.init('app-container');

            // Render the page, passing parameters (like an item ID)
            // We need to pass params correctly, e.g., render(params.id) for detail pages
            // This requires pages to expect the correct parameter structure
            if (route === 'dettaglioAsta' || route === 'offerta') {
                if (params && params.id) {
                    page.render(params.id);
                } else {
                    MessageManager.showErrors("ID asta mancante per la navigazione.");
                    this.navigateTo(this.defaultRoute); // Redirect on error
                }
            } else if (route === 'ricercaAste') {
                // Pass search term if present
                page.render(params.parolaChiave);
            }
            else {
                // For pages without specific parameters needed by render directly
                page.render(params);
            }

            // Update URL hash (optional, for linkability/bookmarking)
            const queryString = Object.keys(params).map(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    return `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`;
                }
                return null;
            }).filter(p => p !== null).join('&');

            window.history.pushState({}, '', `#${route}${queryString ? '?' + queryString : ''}`);


        } else {
            console.error(`Route "${route}" not found.`);
            MessageManager.showErrors(`Pagina non trovata: ${route}`);
            this.navigateTo(this.defaultRoute); // Redirect to default if route is invalid
        }
    }
};

// Initialize the app when the DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});