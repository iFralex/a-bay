/**
 * Pagina di dettaglio di un'asta
 */
const DettaglioAstaPage = {
    container: null,
    asta: null,
    vincitore: null,
    isVenditore: false,

    /**
     * Inizializza la pagina
     * @param {string} containerId - ID del contenitore HTML per il contenuto
     */
    init: function (containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) {
            console.error(`Container con ID "${containerId}" non trovato`);
            return;
        }
    },

    /**
     * Carica i dati dell'asta
     * @param {number} id - ID dell'asta da visualizzare
     */
    loadData: async function (id) {
        try {
            const data = await API.get('/dettaglioAsta', { id });
            this.asta = data.asta || null;
            if (this.asta) {
                this.asta.venditore = this.asta.offerte?.[0]?.username;
                this.asta.prezzoIniziale = this.asta.offerte?.[0]?.prezzo;
                this.asta.offerteSenzaVenditore = this.asta.offerte?.splice(1).reverse();
                this.asta.offertaMassima = this.asta.offerteSenzaVenditore && this.asta.offerteSenzaVenditore.length > 0 ? this.asta.offerteSenzaVenditore[this.asta.offerteSenzaVenditore.length - 1] : null
            }
            this.vincitore = data.vincitore || null;

            // Verifica se l'utente corrente è il venditore
            const user = App.getCurrentUser(); // Assumi che App.getCurrentUser restituisca l'utente loggato
            this.isVenditore = user && this.asta && user.username === this.asta.venditore;

            return true;
        } catch (errors) {
            MessageManager.showErrors(errors.map(e => e.message || 'Errore nel caricamento dei dati dell\'asta'));
            return false;
        }
    },

    /**
     * Renderizza la pagina
     * @param {number} id - ID dell'asta da visualizzare
     */
    render: async function (id) {
        if (!this.container) return;

        // Carica i dati dell'asta
        const success = await this.loadData(id);
        if (!success || !this.asta) {
            this.container.innerHTML = `
                <main class="auction-detail">
                    <p class="not-found">Asta non trovata o non visualizzabile.</p>
                </main>
            `;
            return;
        }

        // Crea la struttura HTML della pagina
        let html = `
            <main class="auction-detail">
                <h1 class="auction-title">Asta #${this.asta.id}: ${this.asta.nome}</h1>
                
                ${this.asta.encodedImage ?
                `<img class="auction-image" src="data:image/jpeg;base64,${this.asta.encodedImage}" alt="Immagine asta" />` :
                ''}
                
                <p class="auction-description">${this.asta.descrizione}</p>
                
                <div class="info-grid">
                    <div><strong>Venditore:</strong> ${this.asta.venditore}</div>
                    <div><strong>Prezzo iniziale:</strong> €${this.asta.prezzoIniziale}</div>
                    <div>
                        <strong>Offerta Massima:</strong>
                        ${this.asta.offertaMassima ?
                `${this.asta.offertaMassima.username} | €${this.asta.offertaMassima.prezzo}` :
                '-- | --'}
                    </div>
                    <div><strong>Rialzo minimo:</strong> €${this.asta.rialzoMinimo}</div>
                    <div><strong>Scadenza:</strong> ${this.asta.formattedScadenza}</div>
                    <div>
                        <strong>Stato:</strong> 
                        <span class="badge ${this.asta.chiusa ? 'closed' : 'open'}">
                            ${this.asta.chiusa ? 'Chiusa' : 'Aperta'}
                        </span>
                    </div>
                </div>
                
                <section class="section">
                    <div class="section-header">
                        <h2>Articoli inclusi</h2>
                    </div>
                    <div class="scroll-row" id="articoli-inclusi">
                        ${this.asta.articoli && this.asta.articoli.length ?
                '' :
                '<p class="no-results">Nessun articolo incluso.</p>'}
                    </div>
                </section>
        `;

        // Sezione vincitore (se l'asta è chiusa e c'è un vincitore)
        if (this.vincitore) {
            html += `
                <section class="winner-section">
                    <h3>Vincitore</h3>
                    <p><strong>Username:</strong> ${this.vincitore.username}</p>
                    <p><strong>Nome:</strong> ${this.vincitore.nome} ${this.vincitore.cognome}</p>
                    <p><strong>Prezzo finale:</strong> €${this.asta.offertaMassima ? this.asta.offertaMassima.prezzo : 'N/D'}</p>
                    <p><strong>Indirizzo:</strong> ${this.vincitore.indirizzo}</p>
                    <p><strong>Costo spedizione:</strong> €5</p>
                </section>
            `;
        }

        // Sezione offerte
        html += `
                <section class="offerte-section">
                    <div class="section-header">
                        <h2>Offerte ricevute</h2>
                    </div>
                    <table class="offerte-table">
                        <thead>
                            <tr>
                                <th>Utente</th>
                                <th>Importo</th>
                                <th>Data/Ora</th>
                            </tr>
                        </thead>
                        <tbody id="offerte-table-body">
                            ${this.asta.offerteSenzaVenditore && this.asta.offerteSenzaVenditore.length ?
                '' :
                '<tr><td colspan="3">Nessuna offerta ricevuta.</td></tr>'}
                        </tbody>
                    </table>
                </section>
        `;

        // Form per chiudere l'asta (solo se l'utente è il venditore e l'asta è aperta)
        if (this.isVenditore && !this.asta.chiusa) {
            html += `
                <form class="chiudi-asta-form" id="form-chiudi-asta">
                    <input type="hidden" name="id" value="${this.asta.id}" />
                    <input type="hidden" name="aggiudicatario" value="${this.asta.offertaMassima ? this.asta.offertaMassima.username : this.asta.venditore}" />
                    <button type="submit">Chiudi Asta</button>
                </form>
            `;
        }

        html += `</main>`;

        this.container.innerHTML = html;

        // Aggiunge gli articoli inclusi
        if (this.asta.articoli && this.asta.articoli.length) {
            const articoliContainer = document.getElementById('articoli-inclusi');
            articoliContainer.innerHTML = '';

            this.asta.articoli.forEach(articolo => {
                const scrollItem = document.createElement('div');
                scrollItem.className = 'scroll-item';
                scrollItem.appendChild(ArticoloCard.create(articolo));
                articoliContainer.appendChild(scrollItem);
            });
        }

        // Aggiunge le offerte alla tabella
        if (this.asta.offerteSenzaVenditore && this.asta.offerteSenzaVenditore.length) {
            const offerteTableBody = document.getElementById('offerte-table-body');
            offerteTableBody.innerHTML = '';

            // Estrae e ordina le offerte (escludendo quelle del venditore)
            const offerte = this.asta.offerteSenzaVenditore;

            offerte.forEach(offerta => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${offerta.username}</td>
                    <td>€${offerta.prezzo}</td>
                    <td>${offerta.formattedDate || new Date(offerta.data).toLocaleString()}</td>
                `;
                offerteTableBody.appendChild(row);
            });
        }

        // Aggiunge l'event listener per chiudere l'asta
        const formChiudiAsta = document.getElementById('form-chiudi-asta');
        if (formChiudiAsta) {
            formChiudiAsta.addEventListener('submit', this.handleChiudiAsta.bind(this));
        }
    },

    /**
     * Gestisce la chiusura di un'asta
     * @param {Event} event - Evento submit del form
     */
    handleChiudiAsta: async function (event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        if (!confirm('Sei sicuro di voler chiudere questa asta?')) {
            return;
        }

        try {
            await API.post('/dettaglioAsta', formData);
            MessageManager.showSuccess('Asta chiusa con successo!');

            // Ricarica la pagina per mostrare lo stato aggiornato
            this.render(this.asta.id);
        } catch (errors) {
            MessageManager.showErrors(errors.map(e => e.message || 'Errore durante la chiusura dell\'asta'));
        }
    }
};