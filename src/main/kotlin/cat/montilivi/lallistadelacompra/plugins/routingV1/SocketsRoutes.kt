package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.websockects.EsdevenimentLlista
import cat.montilivi.lallistadelacompra.model.websockects.SessioWebSocket
import cat.montilivi.lallistadelacompra.model.websockects.TipusAccio
import cat.montilivi.lallistadelacompra.repositori.GestorDeConnexions
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText

fun Route.rutesDelsSockets() {
    // És vital que el client enviï el Token, si no, qualsevol podria connectar-se
    webSocket("/ws") {
        // Extraiem la info de l'usuari del Token
        val principal = call.principal<JWTPrincipal>()
        val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt()
            ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No id"))

        val nomUsuari = principal.payload.getClaim("nomUsuari")?.asString() ?: "Usuari"

        // Registrem la sessió al nostre Gestor de connexions
        val connexioNova = SessioWebSocket(idUsuari, nomUsuari, this)
        GestorDeConnexions.afegeix(connexioNova)

        val salutacio = EsdevenimentLlista(
            accio = TipusAccio.CONNEXIO_ESTABLERTA,
            idLlista = 0
        )
        connexioNova.session.sendSerialized(salutacio)

        try {
            // Bucle de manteniment: mentre la connexió estigui oberta,
            // el codi es quedarà "aturat" aquí esperant missatges o el tancament.
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    // De moment només fem un echo o log per saber que funciona
                    println("Missatge rebut de $nomUsuari: $text")
                }
            }
        } catch (e: Exception) {
            println("Error en la connexió de $nomUsuari: ${e.localizedMessage}")
        } finally {
            // 4. Quan el bucle s'acaba (el client marxa), eliminem la sessió
            GestorDeConnexions.elimina(idUsuari)
        }
    }

}