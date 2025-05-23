<jsp:useBean id="asta" scope="request" type="model.Asta"/>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="utils.TimeUtils" %>

<div style="
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    padding: 20px;
    border: 2px solid ${asta.chiusa ? '#ccc' : '#007bff'};
    border-left: 10px solid ${asta.chiusa ? '#888' : '#28a745'};
    border-radius: 10px;
    background-color: ${asta.chiusa ? '#f8f9fa' : '#ffffff'};
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    transition: 0.3s;
    text-decoration: none;
    max-width: 100%;
    overflow: hidden;
">
    <div style="overflow: hidden; width: 100%;">
        <div style="display: flex; flex-direction: row; width: 100%;">
            <c:if test="${not empty asta.immagine}">
                <img src="data:image/jpeg;base64,${asta.immagine}"
                    alt="${asta.nome}"
                    style="width: 120px; height: auto; margin-right: 20px; border-radius: 5px; object-fit: cover;"/>
            </c:if>

            <div style="flex: 1; overflow: hidden;">
                <h2 style="margin: 0; color: ${asta.chiusa ? '#888' : '#333'};">${asta.nome}</h2>
                <p style="margin: 5px 0 10px; font-size: 0.95em; color: #555; max-height: 4.5em; overflow: hidden; line-height: 1.5em;">
                    ${asta.descrizione}
                </p>

                <p style="margin: 5px 0;"><strong>Venditore:</strong> ${asta.venditore}</p>

                <c:if test="${asta.offertaMassima != null}">
                    <p style="margin: 5px 0;"><strong>Offerta massima:</strong> €${asta.offertaMassima.prezzo}</p>
                </c:if>

                <p style="margin: 5px 0;">
                    <strong>Scade tra:</strong> ${TimeUtils.getTempoMancante(asta.scadenza)}
                    <c:if test="${asta.chiusa}">
                        <span style="color: red; font-weight: bold; margin-left: 10px;">[CHIUSA]</span>
                    </c:if>
                </p>
            </div>
        </div>
        <div style="margin-top:1rem;">
            <strong>Articoli inclusi:</strong>
            <div class="scroll-row">
                <c:forEach var="art" items="${asta.articoli}">
                    <div class="scroll-item">
                        <c:set var="articolo" value="${art}" scope="request" />
                        <jsp:include page="articoloCard.jsp" />
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</div>
