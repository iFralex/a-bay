<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<div class="card">
    <img class="card-img" src="data:image/jpeg;base64,${articolo.immagine}" alt="Immagine di ${articolo.nome}" />

    <div class="card-body">
        <h3 class="card-title">${articolo.nome}</h3>
        <p class="card-description">${articolo.descrizione}</p>
        <div class="card-price">€ ${articolo.prezzo}</div>
    </div>
</div>

<style>
.card {
    width: 150px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    margin: 20px;
    font-family: 'Arial', sans-serif;
}

.card-img {
    width: 100%;
    height: 110px;
    object-fit: cover;
}

.card-body {
    padding: 15px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.card-title {
    font-size: 1.1rem;
    font-weight: bold;
    margin: 0 0 8px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.card-description {
    font-size: 0.95rem;
    color: #555;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

.card-price {
    font-size: 1rem;
    font-weight: bold;
    color: #007bff;
}
</style>
