/**
 * Gestore dei messaggi per mostrare notifiche all'utente
 */
const MessageManager = {
    container: null,
    
    /**
     * Inizializza il gestore dei messaggi
     * @param {string} containerId - ID del contenitore HTML per i messaggi
     */
    init: function(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) {
            console.error(`Container dei messaggi con ID "${containerId}" non trovato`);
        }
        this.clearMessages();
    },
    
    /**
     * Rimuove tutti i messaggi
     */
    clearMessages: function() {
        if (this.container) {
            this.container.innerHTML = '';
        }
    },
    
    /**
     * Mostra un messaggio
     * @param {string} text - Testo del messaggio
     * @param {string} type - Tipo di messaggio ('success', 'error', 'warning')
     * @param {number} duration - Durata in millisecondi prima che il messaggio scompaia (0 per non scomparire)
     */
    showMessage: function(text, type = 'success', duration = 5000) {
        if (!this.container) return;
        
        const messageElement = document.createElement('div');
        messageElement.className = `message ${type}`;
        messageElement.textContent = text;
        
        this.container.appendChild(messageElement);
        
        // Rimuovi automaticamente il messaggio dopo la durata specificata
        if (duration > 0) {
            setTimeout(() => {
                messageElement.remove();
            }, duration);
        }
    },
    
    /**
     * Mostra un messaggio di successo
     * @param {string} text - Testo del messaggio
     * @param {number} duration - Durata in millisecondi
     */
    showSuccess: function(text, duration = 5000) {
        this.showMessage(text, 'success', duration);
    },
    
    /**
     * Mostra un messaggio di errore
     * @param {Object} errors - Testi dei messaggio
     * @param {number} duration - Durata in millisecondi
     */
    showErrors: function(errors, duration = 10000) {
        console.log(errors);
        (Array.isArray(errors) ? errors : [errors]).map(e => this.showMessage(e, 'error', duration))
    },
    
    /**
     * Mostra un messaggio di avviso
     * @param {string} text - Testo del messaggio
     * @param {number} duration - Durata in millisecondi
     */
    showWarning: function(text, duration = 5000) {
        this.showMessage(text, 'warning', duration);
    }
};