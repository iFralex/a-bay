/**
 * Pagina di gestione delle aste (Vendo)
 */
const GestioneAstePage = {
    container: null,
    articoliUtente: [],
    asteAperte: [],
    asteChiuse: [],

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
     * Carica i dati necessari per la pagina
     */
    loadData: async function () {
        try {
            const data = await API.get('/vendo');
            this.articoliUtente = data.articoliUtente || [];
            this.asteAperte = data.asteAperte || [];
            this.asteChiuse = data.asteChiuse || [];
            return true;
        } catch (errors) {
            console.log("ee", errors)
            MessageManager.showErrors(errors.map(e => e.message || 'Errore nel caricamento dei dati'));
            return false;
        }
    },

    /**
     * Renderizza la pagina
     */
    render: async function () {
        if (!this.container) return;

        // Carica i dati se non sono già stati caricati
        if (this.articoliUtente.length === 0 && this.asteAperte.length === 0 && this.asteChiuse.length === 0) {
            const success = await this.loadData();
            if (!success) return;
        }

        // Crea la struttura HTML della pagina
        let html = `
            <div class="auction-dashboard">
                <h1 class="page-title">Gestione Aste</h1>
                
                <!-- Sezione Aste -->
                <div class="section">
                    <div class="section-header">
                        <h2>Aste Attive</h2>
                    </div>
                    <div class="card-grid" id="aste-aperte-container">
                        ${this.asteAperte.length === 0 ? '<p class="no-results">Nessuna asta attiva</p>' : ''}
                    </div>
                    
                    <div class="section-header">
                        <h2>Aste Chiuse</h2>
                    </div>
                    <div class="card-grid" id="aste-chiuse-container">
                        ${this.asteChiuse.length === 0 ? '<p class="no-results">Nessuna asta chiusa</p>' : ''}
                    </div>
                </div>
                
                <!-- Sezione Forms -->
                <div class="section forms-section">
                    <div class="form-card">
                    <h2>Nuovo Articolo</h2>
                    <form id="form-articolo">
                    <input type="hidden" name="action" value="createArticolo" />
                    <label for="nome-articolo">Nome</label>
                    <input type="text" name="nome" id="nome-articolo" required minlength="1" maxlength="1000" />
                    <label for="descrizione-articolo">Descrizione</label>
                    <textarea name="descrizione" id="descrizione-articolo" required minlength="1" maxlength="1000"></textarea>
                    <label for="immagine-articolo">Immagine</label>
                    <input type="file" name="immagine" id="immagine-articolo" accept="image/*" required />
                    <label for="prezzo-articolo">Prezzo (€)</label>
                    <input type="number" name="prezzo" id="prezzo-articolo" step="0.01" min="0" max="5000000" required />
                    <button type="submit" class="main-action">Aggiungi Articolo</button>
                    </form>
                    </div>
                    <div class="form-card">
                    <h2>Nuova Asta</h2>
                    <form id="form-asta">
                    <input type="hidden" name="action" value="createAsta" />
                    <label>Articoli da includere</label>
                    <div class="scroll-row" id="articoli-container">
                    ${this.articoliUtente.length === 0 ? '<p class="no-results">Nessun articolo disponibile</p>' : ''}
                    </div>
                    <label for="nome-asta">Nome</label>
                    <input type="text" name="nome" id="nome-asta" required minlength="1" maxlength="1000" />
                    <label for="descrizione-asta">Descrizione</label>
                    <textarea name="descrizione" id="descrizione-asta" required minlength="1" maxlength="1000"></textarea>
                    <label for="immagine-asta">Immagine</label>
                    <input type="file" name="immagine" id="immagine-asta" accept="image/*" required />
                    <label for="rialzo-asta">Rialzo minimo (€)</label>
                    <input type="number" name="rialzo" id="rialzo-asta" min="0" max="5000000" required />
                    <label for="scadenza-asta">Scadenza</label>
                    <input type="datetime-local" name="scadenza" id="scadenza-asta" required />
                    <button type="submit" class="main-action">Crea Asta</button>
                    </form>
                    </div>
                    </div>
            </div>
        `;

        this.container.innerHTML = html;

        // Aggiunge le aste alle sezioni
        const asteAperteContainer = document.getElementById('aste-aperte-container');
        const asteChiuseContainer = document.getElementById('aste-chiuse-container');

        if (this.asteAperte.length > 0) {
            asteAperteContainer.innerHTML = '';
            this.asteAperte.forEach(asta => {
                asteAperteContainer.appendChild(AstaCard.create(asta, (id) => {
                    window.dispatchEvent(new CustomEvent('navigate', {
                        detail: { route: 'dettaglioAsta', params: { id } }
                    }));
                }));
            });
        }

        if (this.asteChiuse.length > 0) {
            asteChiuseContainer.innerHTML = '';
            this.asteChiuse.forEach(asta => {
                asteChiuseContainer.appendChild(AstaCard.create(asta, (id) => {
                    window.dispatchEvent(new CustomEvent('navigate', {
                        detail: { route: 'dettaglioAsta', params: { id } }
                    }));
                }));
            });
        }

        // Aggiunge gli articoli al form di creazione asta
        const articoliContainer = document.getElementById('articoli-container');
        if (this.articoliUtente.length > 0) {
            articoliContainer.innerHTML = '';
            this.articoliUtente.forEach(articolo => {
                const scrollItem = document.createElement('div');
                scrollItem.className = 'scroll-item';

                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.name = 'articoliId';
                checkbox.id = `art-${articolo.id}`;
                checkbox.value = articolo.id;

                const label = document.createElement('label');
                label.htmlFor = `art-${articolo.id}`;
                label.appendChild(ArticoloCard.create(articolo, true));

                scrollItem.appendChild(checkbox);
                scrollItem.appendChild(label);
                articoliContainer.appendChild(scrollItem);
            });
        }

        // Aggiunge gli event listeners per i form
        document.getElementById('form-articolo').addEventListener('submit', this.handleFormArticolo.bind(this));
        document.getElementById('form-asta').addEventListener('submit', this.handleFormAsta.bind(this));
    },

    /**
     * Gestisce la sottomissione del form per creare un articolo
     * @param {Event} event - Evento submit
     */
    handleFormArticolo: async function (event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        try {
            await API.post('/vendo', formData);
            MessageManager.showSuccess('Articolo creato con successo!');
            form.reset();

            // Ricarica i dati e aggiorna la pagina
            await this.loadData();
            this.render();
        } catch (errors) {
            MessageManager.showErrors(errors.map(e => e.message || 'Errore durante la creazione dell\'articolo'));
        }
    },

    /**
     * Gestisce la sottomissione del form per creare un'asta
     * @param {Event} event - Evento submit
     */
    handleFormAsta: async function (event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        // Verifica che almeno un articolo sia selezionato
        const articoliSelezionati = formData.getAll('articoliId');
        if (articoliSelezionati.length === 0) {
            MessageManager.showErrors('Seleziona almeno un articolo da includere nell\'asta');
            return;
        }

        try {
            await API.post('/vendo', formData);
            MessageManager.showSuccess('Asta creata con successo!');
            Cookies.setCookie("ultimaAzione", "creazione-asta");
            form.reset();

            // Ricarica i dati e aggiorna la pagina
            await this.loadData();
            this.render();
        } catch (errors) {
            MessageManager.showErrors(errors.map(e => e.message || 'Errore durante la creazione dell\'asta'));
        }
    }
};