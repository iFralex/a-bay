/**
 * Simple Client-Side Router
 */
const Router = {
    routes: {
        '/acquisto': RicercaAstePage,
        '/vendo': GestioneAstePage,
        '/dettaglioAsta/:id': DettaglioAstaPage,
        // Add other routes here, e.g., for making an offer
        // '/offerta/:id': OffertaPage,
    },
    container: null,
    messageContainerId: null, // Keep track of the message container ID

    /**
     * Initializes the router and event listeners.
     * @param {string} containerId - ID of the main content container.
     * @param {string} messageContainerId - ID of the message container.
     */
    init: function(containerId, messageContainerId) {
        this.container = document.getElementById(containerId);
        this.messageContainerId = messageContainerId;

        if (!this.container) {
            console.error(`Router container with ID "${containerId}" not found`);
            return;
        }

        // Initialize MessageManager
        MessageManager.init(messageContainerId);

        // Listen for URL changes (browser back/forward buttons)
        window.addEventListener('popstate', this.handleLocationChange.bind(this));

        // Listen for custom 'navigate' events from pages
        window.addEventListener('navigate', (event) => {
            const { route, params } = event.detail;
            this.navigate(route, params);
        });

        // Handle the initial page load
        this.handleLocationChange();
    },

    /**
     * Navigates to a new route.
     * @param {string} route - The route pattern (e.g., '/dettaglioAsta/:id').
     * @param {object} params - Parameters for the route and/or query string.
     */
    navigate: function(route, params = {}) {
        let path = route;

        // Substitute parameters in the path
        for (const key in params) {
            if (path.includes(`:${key}`)) {
                path = path.replace(`:${key}`, params[key]);
                delete params[key]; // Remove from params so they go to query string
            }
        }

        // Add remaining params as query string
        const queryParams = new URLSearchParams(params).toString();
        if (queryParams) {
            path += `?${queryParams}`;
        }

        // Use History API to change URL without full page reload
        history.pushState(params, '', path);

        // Handle the new location
        this.handleLocationChange();
    },

    /**
     * Handles changes to the browser's location.
     * Determines the current route and renders the corresponding page.
     */
    handleLocationChange: async function() {
        const path = window.location.pathname;
        const queryParams = new URLSearchParams(window.location.search);

        // Clear previous messages
        MessageManager.clearMessages();

        let matchedRoute = null;
        let routeParams = {};
        let currentPage = null;

        // Find the matching route
        for (const routePattern in this.routes) {
            // Simple matching: Check if path starts with the pattern (non-parameterized)
            // or if it matches a parameterized pattern
            const patternSegments = routePattern.split('/');
            const pathSegments = path.split('/');

            if (patternSegments.length === pathSegments.length) {
                 let match = true;
                 let currentParams = {};
                 for (let i = 0; i < patternSegments.length; i++) {
                     const patternSegment = patternSegments[i];
                     const pathSegment = pathSegments[i];

                     if (patternSegment.startsWith(':')) {
                         // This segment is a parameter
                         const paramName = patternSegment.substring(1);
                         currentParams[paramName] = pathSegment;
                     } else if (patternSegment !== pathSegment) {
                         // Non-parameterized segments don't match
                         match = false;
                         break;
                     }
                 }

                 if (match) {
                     matchedRoute = routePattern;
                     routeParams = currentParams;
                     currentPage = this.routes[routePattern];
                     break; // Found a match, stop searching
                 }
            } else if (routePattern === path) {
                 // Exact match for non-parameterized routes
                 matchedRoute = routePattern;
                 currentPage = this.routes[routePattern];
                 break;
            }
        }

        // Add query parameters to routeParams
        for (const [key, value] of queryParams.entries()) {
            routeParams[key] = value;
        }


        if (currentPage) {
            // Initialize the page and render
            currentPage.init(this.container.id);
            // Pass route and query parameters to render
            await currentPage.render(routeParams);
        } else {
            // No route found, render a 404 or redirect
            console.warn(`Route not found: ${path}`);
            this.container.innerHTML = '<main class="page-not-found"><h1>404 - Pagina non trovata</h1><p>La pagina che stai cercando non esiste.</p></main>';
             // Optionally redirect to a default page
            // this.navigate('/acquisto');
        }
    },

     /**
      * Gets the current route path.
      * @returns {string}
      */
     getCurrentPath: function() {
         return window.location.pathname + window.location.search;
     }
};