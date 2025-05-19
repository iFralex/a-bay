/**
 * Modulo per gestire le chiamate API REST
 */
const API = {
    /**
     * Esegue una richiesta GET all'API
     * @param {string} endpoint - L'endpoint dell'API
     * @param {Object} params - Parametri query string opzionali
     * @returns {Promise<Object>} - La risposta JSON dell'API
     */
    get: async function (endpoint, params = {}) {
        try {
            // Costruisci la query string se ci sono parametri
            const queryString = Object.keys(params).length > 0
                ? '?' + new URLSearchParams(params).toString()
                : '';

            const response = await fetch(`/api${endpoint}${queryString}`);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || data.errors || 'Si è verificato un errore');
            }

            return data;
        } catch (error) {
            console.error('Errore nella richiesta GET:', Array.isArray(error) ? error.join(" | ") : error);
            console.log(Array.isArray(error) ? error : [error], "cccc")
            throw Array.isArray(error) ? error : [error];
        }
    },

    /**
     * Esegue una richiesta POST all'API
     * @param {string} endpoint - L'endpoint dell'API
     * @param {Object|FormData} body - I dati da inviare
     * @returns {Promise<Object>} - La risposta dell'API
     */
    post: async function (endpoint, body = {}) {
        try {
            let options = {
                method: 'POST',
            };
            console.log("body", body)
            // Gestione diversa se body è FormData o un oggetto
            if (body instanceof FormData) {
                options.body = body;
            } else {
                options.headers = {
                    'Content-Type': 'application/json'
                };
                options.body = JSON.stringify(body);
            }

            const response = await fetch(`/api${endpoint}`, options);

            // Verifica se la risposta ha un contenuto JSON
            const contentType = response.headers.get('content-type');
            let data;

            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = { success: response.ok };
            }

            if (!response.ok) {
                throw new Error(data.error || data.errors || 'Si è verificato un errore');
            }

            return data;
        } catch (error) {
            console.error('Errore nella richiesta POST:', Array.isArray(error) ? error.join(" | ") : error);
            throw Array.isArray(error) ? error : [error];
        }
    }
};