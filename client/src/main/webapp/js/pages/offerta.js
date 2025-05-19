/**
 * Pagina di dettaglio di un'asta per fare un'offerta
 */
const OffertaPage = {
    container: null,
    asta: null,
    astaId: null, // Store the ID of the currently displayed auction

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

        // Clear previous content
        this.container.innerHTML = '';
    },

    /**
     * Carica i dati dell'asta
     * @param {number} id - ID dell'asta da visualizzare
     */
    loadData: async function (id) {
        this.astaId = id; // Store the ID
        try {
            // Assume API endpoint /offerta responds with auction details
            const data = await API.get('/offerta', { id: id });
            this.asta = data.asta || null;
            if (this.asta) {
                this.asta.venditore = this.asta.offerte?.[0]?.username;
                this.asta.prezzoIniziale = this.asta.offerte?.[0]?.prezzo;
                this.asta.offerteSenzaVenditore = this.asta.offerte?.splice(1).reverse();
                this.asta.offertaMassima = this.asta.offerteSenzaVenditore && this.asta.offerteSenzaVenditore.length > 0 ? this.asta.offerteSenzaVenditore[this.asta.offerteSenzaVenditore.length - 1] : null
                console.log(this.asta.offertaMassima, this.asta.offerteSenzaVenditore)
            }
            // The API might return the winner if the auction is closed,
            // but for bidding page, we primarily need auction and offer details.
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

        const currentUser = App.getCurrentUser();

        if (currentUser && currentUser.username)
            this.registraAstaVisitata(id, currentUser.username);

        // Determine if the user can make an offer
        const canMakeOffer = currentUser &&
            currentUser.username !== null &&
            !this.asta.chiusa &&
            currentUser.username !== this.asta.venditore;

        // Determine the current minimum bid
        const currentMaxBid = this.asta.offertaMassima ? this.asta.offertaMassima.prezzo : this.asta.prezzoIniziale;
        const minNextBid = currentMaxBid + this.asta.rialzoMinimo;

        // Create the HTML structure
        let html = `
            <main class="auction-detail">
                <h1 class="auction-title">Asta #${this.asta.id}: ${this.asta.nome}</h1>

                <div class="auction-info">
                    ${this.asta.encodedImage ?
                `<img class="auction-image" src="data:image/jpeg;base64,${this.asta.encodedImage}" alt="Immagine asta" />` :
                ''}
                    <p class="auction-description">${this.asta.descrizione}</p>
                    <p><strong>Scadenza:</strong> ${this.asta.formattedScadenza}</p>
                     <p><strong>Prezzo iniziale:</strong> €${this.asta.prezzoIniziale}</p>
                     <p>
                        <strong>Offerta Massima Attuale:</strong>
                        ${this.asta.offertaMassima ?
                `${this.asta.offertaMassima.username} | €${this.asta.offertaMassima.prezzo}` :
                'Ancora nessuna offerta'}
                    </p>
                    <p><strong>Rialzo minimo:</strong> €${this.asta.rialzoMinimo}</p>
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

                <section class="section">
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

        // Form for submitting a new offer (only if allowed)
        if (canMakeOffer) {
            html += `
                <section class="section">
                    <div class="section-header">
                        <h2>Inserisci una nuova offerta</h2>
                    </div>
                    <form id="form-offerta" class="form">
                        <input type="hidden" name="astaId" value="${this.asta.id}" />
                        <div class="form-group">
                            <label for="prezzo">Prezzo (€) (Offerta minima: €${minNextBid}):</label>
                            <input type="number" step="1" name="prezzo" id="prezzo" min="${minNextBid}" required class="form-control" />
                        </div>
                        <button type="submit" class="button main-action">Invia Offerta</button>
                    </form>
                </section>
            `;
        } else if (this.asta.chiusa) {
            html += `<p class="info-message">Quest'asta è chiusa.</p>`;
        } else if (!currentUser) {
            html += `<p class="info-message">Accedi per fare un'offerta.</p>`;
        } else if (currentUser.username === this.asta.venditore) {
            html += `<p class="info-message">Non puoi fare offerte sulla tua stessa asta.</p>`;
        }


        html += `</main>`;

        this.container.innerHTML = html;

        // Add included articles
        if (this.asta.articoli && this.asta.articoli.length) {
            const articoliContainer = document.getElementById('articoli-inclusi');
            articoliContainer.innerHTML = ''; // Clear initial "no results" message if present
            this.asta.articoli.forEach(articolo => {
                const scrollItem = document.createElement('div');
                scrollItem.className = 'scroll-item';
                scrollItem.appendChild(ArticoloCard.create(articolo));
                articoliContainer.appendChild(scrollItem);
            });
        }

        // Add offers to the table
        if (this.asta.offerteSenzaVenditore && this.asta.offerteSenzaVenditore.length) {
            const offerteTableBody = document.getElementById('offerte-table-body');
            offerteTableBody.innerHTML = ''; // Clear initial "no results" message if present

            // Filter out vendor's offers and sort by date descending
            const offerteUtente = this.asta.offerteSenzaVenditore;

            if (offerteUtente.length > 0) {
                offerteUtente.forEach(offerta => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${offerta.username}</td>
                        <td>€${offerta.prezzo}</td>
                        <td>${offerta.formattedDate || new Date(offerta.data).toLocaleString()}</td>
                    `;
                    offerteTableBody.appendChild(row);
                });
            } else {
                const row = document.createElement('tr');
                row.innerHTML = `<td colspan="3">Nessuna offerta ricevuta dagli utenti.</td>`;
                offerteTableBody.appendChild(row);
            }
        }

        // Add event listener for the offer form
        const formOfferta = document.getElementById('form-offerta');
        if (formOfferta) {
            formOfferta.addEventListener('submit', this.handleFormOfferta.bind(this));
        }
    },

    /**
     * Gestisce la sottomissione del form per fare un'offerta
     * @param {Event} event - Evento submit
     */
    handleFormOfferta: async function (event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        // Basic client-side validation (rely on backend for definitive check)
        const prezzoInput = form.elements['prezzo'];
        const prezzo = parseInt(prezzoInput.value);
        if (isNaN(prezzo)) {
            MessageManager.showErrors('Inserisci un importo valido.');
            return;
        }
        const currentMaxBid = this.asta.offertaMassima ? this.asta.offertaMassima.prezzo : this.asta.prezzoIniziale;
        const minNextBid = currentMaxBid + this.asta.rialzoMinimo;
        if (prezzo < minNextBid) {
            MessageManager.showErrors(`L'offerta deve essere almeno €${minNextBid}.`);
            return;
        }


        try {
            // Assume API endpoint /offerta handles POST requests for bids
            await API.post('/offerta', formData);
            MessageManager.showSuccess('Offerta inviata con successo!');
            form.reset();

            // Ricarica i dati e aggiorna la pagina per mostrare la nuova offerta
            await this.loadData(this.astaId); // Use the stored ID
            this.render(this.astaId); // Re-render using the stored ID

        } catch (errors) {
            MessageManager.showErrors(errors.map(e => e.message || 'Errore durante l\'invio dell\'offerta'));
        }
    },

    registraAstaVisitata: function (idAsta, username) {
        if (!username) return
        let visitate = Cookies.getCookie("asteVisitate-" + username);
        let ids = visitate ? visitate.split(",") : [];
        if (!ids.includes(idAsta.toString())) {
            ids.push(idAsta);
            Cookies.setCookie("asteVisitate-" + username, ids.join(","));
        }
    }
};