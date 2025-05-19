import { fetchDatiVendo, creaArticolo, creaAsta } from "./utils/api.js";
import { renderAste, renderArticoliCheckbox } from "./router.js";

export function initVendo() {
  document.getElementById("formArticolo").addEventListener("submit", async e => {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);

    try {
      await creaArticolo(formData);
      alert("Articolo aggiunto!");
      form.reset();
      caricaDati();
    } catch (err) {
      alert(err.errors?.join("\n") || "Errore generico");
    }
  });

  document.getElementById("formAsta").addEventListener("submit", async e => {
    e.preventDefault();
    const form = e.target;
    const formData = new FormData(form);

    try {
      await creaAsta(formData);
      alert("Asta creata!");
      form.reset();
      caricaDati();
    } catch (err) {
      alert(err.errors?.join("\n") || "Errore generico");
    }
  });

  caricaDati();
}

async function caricaDati() {
  try {
    const data = await fetchDatiVendo();
    renderAste(data.asteAperte, data.asteChiuse);
    renderArticoliCheckbox(data.articoliUtente);
  } catch (err) {
    alert(err.message);
  }
}
