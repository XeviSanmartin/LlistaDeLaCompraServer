package cat.montilivi.lallistadelacompra.repositori

import cat.montilivi.lallistadelacompra.model.websockets.EsdevenimentLlista
import cat.montilivi.lallistadelacompra.model.bbdd.ProducteDeLaLlista
import cat.montilivi.lallistadelacompra.model.websockets.SessioWebSocket
import cat.montilivi.lallistadelacompra.model.websockets.TipusAccio
import io.ktor.server.websocket.sendSerialized
import java.util.concurrent.ConcurrentHashMap

object GestorDeConnexions {
    // Fem servir un ConcurrentHashMap perquè molts usuaris es poden
    // connectar/desconnectar a la vegada de forma asíncrona.
    private val sessions = ConcurrentHashMap<Int, SessioWebSocket>()

    // Afegeix una sessió nova
    fun afegeix(sessio: SessioWebSocket) {
        sessions[sessio.idUsuari] = sessio
        println("Connexió establerta: Usuari ${sessio.idUsuari} (${sessio.nomUsuari})")
    }

    // Elimina una sessió (quan es desconnecten)
    fun elimina(idUsuari: Int) {
        sessions.remove(idUsuari)
        println("Connexió tancada: Usuari $idUsuari")
    }

    // Obte la sessió d'un usuari concret (per enviar-li un missatge privat)
    fun obtenSessio(idUsuari: Int): SessioWebSocket? = sessions[idUsuari]

    // Obte totes les sessions (per enviar un missatge global)
    fun totesLesSessions(): List<SessioWebSocket> = sessions.values.toList()

    suspend fun enviaAUsuarisConcrets(idsUsuaris: List<Int>, esdeveniment: EsdevenimentLlista) {
        idsUsuaris.forEach { id ->
            sessions[id]?.let { sessioUsuari ->
                try {
                    // Gràcies al convertidor que hem instal·lat, enviem l'objecte directament
                    sessioUsuari.session.sendSerialized(esdeveniment)
                } catch (e: Exception) {
                    println("Error enviant missatge a $id: ${e.message}")
                }
            }
        }
    }

    suspend fun notificaCanviLlista(idLlista: Int, esdeveniment: EsdevenimentLlista) {
        // Obtenim qui ha de rebre la notícia (aquesta funció va al repositori)
        val propietaris = RepositoriLlistesDeLaCompra.obtenIdsPropietaris(idLlista)

        // Enviem a tots els que estiguin connectats
        enviaAUsuarisConcrets(propietaris, esdeveniment)
    }

// Dins de ConnectionManager.kt

    /**
     * Funció mestra per notificar canvis en una llista a tots els seus propietaris
     */
    suspend fun notificaAccio(
        accio: TipusAccio,
        idLlista: Int,
        idRecursAfectat: Int? = null,
        producte: ProducteDeLaLlista? = null
    ) {
        // Busquem els IDs dels propietaris de la llista al repositori
        val idsPropietaris = RepositoriLlistesDeLaCompra.obtenIdsPropietaris(idLlista)

        // 2. Creem l'objecte que compleix el "contracte"
        val esdeveniment = EsdevenimentLlista(
            accio = accio,
            idLlista = idLlista,
            idRecursAfectat = idRecursAfectat,
            producte = producte
        )

        // Cridem a la funció d'enviament que ja teníem
        enviaAUsuarisConcrets(idsPropietaris, esdeveniment)
    }
}