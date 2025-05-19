/**
 * Componente per la visualizzazione delle carte delle aste
 */
const AstaCard = {
    /**
     * Crea l'HTML per una carta asta
     * @param {Object} asta - Oggetto asta con le proprietà
     * @param {Function|null} clickHandler - Funzione da eseguire al click (opzionale)
     * @returns {HTMLElement} - Elemento HTML della carta
     */
    create: function (asta, clickHandler = null) {
        // Creo l'elemento principale della carta
        const card = document.createElement('div');
        card.className = 'asta-card';
        card.dataset.id = asta.id; // Memorizzo l'ID dell'asta come attributo data

        // Se è stato fornito un handler per il click, aggiungo un event listener
        if (clickHandler) {
            card.style.cursor = 'pointer';
            card.addEventListener('click', () => clickHandler(asta.id));
        }

        // Contenuto della carta
        let cardHTML = '';
        // Immagine dell'asta (se presente)
        if (asta.encodedImage) {
            cardHTML += `<img src="data:image/jpeg;base64,${asta.encodedImage}" alt="${asta.nome}" class="card-image">`;
        } else {
            cardHTML += `<div class="card-image placeholder-image"></div>`;
        }

        // Contenuto testuale
        cardHTML += `<div class="asta-card enhanced ${asta.chiusa ? 'closed' : ''}">
  <div class="card-container">
    <div class="card-header">
      ${asta.immagine ? `<img src="data:image/jpeg;base64,${asta.immagine}" alt="${asta.nome}" class="card-thumbnail" />` : ''}
      <div class="card-title-section">
        <h3 class="card-title">${asta.nome}</h3>
        <span class="badge ${asta.chiusa ? 'closed' : 'open'}">${asta.chiusa ? 'Chiusa' : 'Aperta'}</span>
      </div>
    </div>
    <div class="card-content">
      <p class="card-description">${this.truncateText(asta.descrizione, 100)}</p>
      <div class="card-details">
        <div class="card-info">
          <span class="card-label">Venditore:</span>
          <span class="card-value">${asta.venditore || asta.offerte && asta.offerte.length ? asta.offerte[0].username : ""}</span>
        </div>
        ${asta.offertaMassima ? `
          <div class="card-info">
            <span class="card-label">Offerta massima:</span>
            <span class="card-price highlight">€${asta.offertaMassima.prezzo}</span>
          </div>
        ` : ''}
        <div class="card-info">
          <span class="card-label">Scade tra:</span>
          <span class="card-value">${asta.tempoRimasto || 'N/D'}</span>
        </div>
      </div>
      ${asta.articoli && asta.articoli.length > 0 ? `
        <div class="card-articles">
          <span class="card-label">Articoli inclusi:</span>
          <div class="scroll-row">
            ${asta.articoli.map(art => `
              <div class="scroll-item">
                <!-- Qui dovresti implementare un metodo per renderizzare l'articolo -->
                <div class="articolo-card">
                  <div class="card-content">
                    <h4 class="card-title">${art.nome}</h4>
                    <p class="card-price">€${art.prezzo || '0'}</p>
                  </div>
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      ` : ''}
    </div>
  </div>
</div>`;

        card.innerHTML = cardHTML;
        return card;
    },

    /**
     * Tronca il testo se supera una certa lunghezza
     * @param {string} text - Testo da troncare
     * @param {number} maxLength - Lunghezza massima
     * @returns {string} - Testo troncato
     */
    truncateText: function (text, maxLength) {
        if (!text) return '';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }
};