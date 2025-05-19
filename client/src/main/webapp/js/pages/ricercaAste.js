/**
 * Pagina di ricerca aste
 */
const RicercaAstePage = {
    container: null,
    asteAperte: [],
    asteAggiudicate: [],
    asteVisitate: [],
    parolaChiave: '',

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
     * Carica i dati delle aste
     * @param {string} parolaChiave - Parola chiave per la ricerca (opzionale)
     */
    loadData: async function (parolaChiave = '') {
        try {
            // Costruisci i parametri per la richiesta
            const params = { asteVisitateIds: this.getAsteVisitateString() };
            if (parolaChiave) {
                params.parolaChiave = parolaChiave;
            }


            const data = await API.get('/acquisto', params);
            this.asteAperte = data.asteAperte || [];
            this.asteAggiudicate = data.asteAggiudicate || [];
            this.parolaChiave = data.parolaChiave || parolaChiave;
            this.asteVisitate = data.asteVisitate || [];
            this.sostituisciAsteVisitate(this.asteVisitate.map(a => a.id));
            return true;
        } catch (errors) {
            MessageManager.showErrors(errors.map(e => e.message || 'Errore nel caricamento delle aste'));
            return false;
        }
    },

    /**
     * Renderizza la pagina
     * @param {string} parolaChiave - Parola chiave per la ricerca (opzionale)
     */
    render: async function (parolaChiave = '') {
        if (!this.container) return;

        // Carica i dati se necessario
        if (parolaChiave || this.asteAperte.length === 0) {
            const success = await this.loadData(parolaChiave);
            if (!success) return;
        }

        // Crea la struttura HTML della pagina
        let html = `
            <main class="auction-detail">
                <h1 class="page-title">Ricerca Aste</h1>
                
                <form id="form-ricerca" class="search-form">
                    <input type="text" name="parolaChiave" value="${this.parolaChiave}" 
                           placeholder="Inserisci parola chiave" class="form-control" />
                    <button type="submit" class="button main-action">Cerca</button>
                </form>
        `;

        // Sezione aste aperte
        if (this.asteAperte && this.asteAperte.length > 0) {
            html += `
                <section class="section">
                    <div class="section-header">
                        <h2>Aste aperte trovate</h2>
                    </div>
                    <div class="card-grid" id="aste-aperte-search-container"></div>
                </section>
            `;
        } else if (this.parolaChiave) {
            html += `
                <section class="section">
                    <div class="section-header">
                        <h2>Aste aperte trovate</h2>
                    </div>
                    <p class="no-results">Nessuna asta trovata per "${this.parolaChiave}"</p>
                </section>
            `;
        }

        // Sezione aste aggiudicate (solo per utenti autenticati)
        const currentUser = App.getCurrentUser();
        if (currentUser) {
            html += `
                <section class="section">
                    <div class="section-header">
                        <h2>Aste vinte</h2>
                    </div>
            `;

            if (this.asteAggiudicate && this.asteAggiudicate.length > 0) {
                html += `<div class="card-grid" id="aste-aggiudicate-container"></div>`;
            } else {
                html += `<p class="no-results">Non hai ancora vinto nessuna asta.</p>`;
            }

            html += `</section>`;

            //Aste visitate
            if (this.asteVisitate && this.asteVisitate.length > 0)
                html += `
                <section class="section">
                    <div class="section-header">
                        <h2>Aste visitate ultimamente</h2>
                    </div>
                    <div class="card-grid" id="aste-visitate-container"></div>
                </section>
            `;
        }

        html += `</main>`;

        this.container.innerHTML = html;

        // Popola il container delle aste aperte trovate
        const asteAperteContainer = document.getElementById('aste-aperte-search-container');
        if (asteAperteContainer && this.asteAperte.length > 0) {
            asteAperteContainer.innerHTML = '';
            this.asteAperte.forEach(asta => {
                const astaCard = AstaCard.create(asta, (astaId) => {
                    window.dispatchEvent(new CustomEvent('navigate', {
                        detail: { route: 'offerta', params: { id: astaId } }
                    }));
                });
                asteAperteContainer.appendChild(astaCard);
            });
        }

        // Popola il container delle aste aggiudicate (vinte)
        const asteAggiudicateContainer = document.getElementById('aste-aggiudicate-container');
        if (asteAggiudicateContainer && this.asteAggiudicate.length > 0) {
            asteAggiudicateContainer.innerHTML = '';
            this.asteAggiudicate.forEach(asta => {
                const astaCard = AstaCard.create(asta, (astaId) => {
                    window.dispatchEvent(new CustomEvent('navigate', {
                        detail: { route: 'offerta', params: { id: astaId } }
                    }));
                });
                asteAggiudicateContainer.appendChild(astaCard);
            });
        }

        // Popola il container delle aste visitate
        const asteVisitateContainer = document.getElementById('aste-visitate-container');
        if (asteVisitateContainer && this.asteVisitate.length > 0) {
            asteVisitateContainer.innerHTML = '';
            this.asteVisitate.forEach(asta => {
                const astaCard = AstaCard.create(asta, (astaId) => {
                    window.dispatchEvent(new CustomEvent('navigate', {
                        detail: { route: 'offerta', params: { id: astaId } }
                    }));
                });
                asteVisitateContainer.appendChild(astaCard);
            });
        }



        // Aggiungi event listener per il form di ricerca
        const formRicerca = document.getElementById('form-ricerca');
        if (formRicerca) {
            formRicerca.addEventListener('submit', this.handleSearchSubmit.bind(this));
        }
    },

    /**
     * Gestisce l'invio del form di ricerca
     * @param {Event} event - Evento submit
     */
    handleSearchSubmit: function (event) {
        event.preventDefault();
        const formData = new FormData(event.target);
        const parolaChiave = formData.get('parolaChiave') || '';

        // Renderizza nuovamente la pagina con la parola chiave
        this.render(parolaChiave);
    },

    getAsteVisitateString: function () {
        return Cookies.getCookie("asteVisitate-" + App.getCurrentUser()?.username) || "";
    },

    sostituisciAsteVisitate: function (ids) {
        if (!Array.isArray(ids)) return;
        const validIds = ids.map(id => id.toString()).filter(id => id); // filtra eventuali valori vuoti
        Cookies.setCookie("asteVisitate", validIds.join(","));
    }

};