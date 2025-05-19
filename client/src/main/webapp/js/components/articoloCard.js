/**
 * Componente per la visualizzazione delle carte degli articoli
 */
const ArticoloCard = {
    /**
     * Crea l'HTML per una carta articolo
     * @param {Object} articolo - Oggetto articolo con le proprietà
     * @param {boolean} selectable - Se la carta deve essere selezionabile
     * @param {boolean} selected - Se la carta è già selezionata
     * @returns {HTMLElement} - Elemento HTML della carta
     */
    create: function(articolo, selectable = false, selected = false) {
        // Creo l'elemento principale della carta
        const card = document.createElement('div');
        card.className = 'articolo-card';
        card.dataset.id = articolo.id; // Memorizzo l'ID dell'articolo come attributo data
        
        if (selectable) {
            card.classList.add('selectable');
            if (selected) {
                card.classList.add('selected');
            }
        }
        
        // Contenuto della carta
        let cardHTML = '';
        
        // Immagine dell'articolo (se presente)
        if (articolo.immagine) {
            cardHTML += `<img src="data:image/jpeg;base64,${articolo.immagine}" alt="${articolo.nome}" class="card-image">`;
        } else {
            cardHTML += `<div class="card-image placeholder-image"></div>`;
        }
        
        // Contenuto testuale
        cardHTML += `
            <div class="card-content">
                <h3 class="card-title">${articolo.nome}</h3>
                <p class="card-description">${this.truncateText(articolo.descrizione, 80)}</p>
                <div class="card-info">
                    <span class="card-price">€${articolo.prezzo || '0'}</span>
                </div>
            </div>
        `;
        
        card.innerHTML = cardHTML;
        return card;
    },
    
    /**
     * Tronca il testo se supera una certa lunghezza
     * @param {string} text - Testo da troncare
     * @param {number} maxLength - Lunghezza massima
     * @returns {string} - Testo troncato
     */
    truncateText: function(text, maxLength) {
        if (!text) return '';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }
};