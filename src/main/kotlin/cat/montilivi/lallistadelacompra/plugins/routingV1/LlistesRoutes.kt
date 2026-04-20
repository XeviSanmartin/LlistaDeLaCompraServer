package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.PeticioLlista
import cat.montilivi.lallistadelacompra.repositori.RepositoriLlistesDeLaCompra
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.rutesDeLesLlistesDeLaCompra(){
    route("llistes"){
        get {
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt()

            if (idUsuari != null) {
                val llistes = RepositoriLlistesDeLaCompra.cercaLlistesPerPropietari(idUsuari)
                call.respond(llistes)
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        // POST: Creació d'una llista nova
        post {
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt()

            // Llegim el nom de la llista del JSON que ens envia el client
            val request = call.receive<PeticioLlista>()

            if (idUsuari != null) {
                val idLlista = RepositoriLlistesDeLaCompra.creaLlista(request.nom, idUsuari)
                call.respond(HttpStatusCode.Created, mapOf("idLlista" to idLlista))
            }
        }

        // PATCH: Actualitzar el nom d'una llista específica
        // URL: /llistes/7
        patch("/{idLlista}") {
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt() ?: return@patch call.respond(HttpStatusCode.Unauthorized)

            val llistaId = call.parameters["idLlista"]?.toIntOrNull() ?: return@patch call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<PeticioLlista>() // Reutilitzem el model que té el camp "nom"

            val exit = RepositoriLlistesDeLaCompra.actualitzaNomLlista(llistaId, request.nom, idUsuari)

            if (exit) {
                call.respond(HttpStatusCode.OK, mapOf("status" to "Llista actualitzada"))
            } else {
                call.respond(HttpStatusCode.NotFound, "Llista no trobada o no tens permís")
            }
        }

        // DELETE: Esborrar una llista
        // URL: /llistes/7
        delete("/{idLlista}") {
            val principal = call.principal<JWTPrincipal>()
            val idUsuari = principal?.payload?.getClaim("idUsuari")?.asInt() ?: return@delete call.respond(HttpStatusCode.Unauthorized)

            val llistaId = call.parameters["idLlista"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val exit = RepositoriLlistesDeLaCompra.eliminaLlista(llistaId, idUsuari)

            if (exit) {
                call.respond(HttpStatusCode.OK, mapOf("status" to "Llista esborrada"))
            } else {
                call.respond(HttpStatusCode.NotFound, "Llista no trobada o no tens permís")
            }
        }
    }
}