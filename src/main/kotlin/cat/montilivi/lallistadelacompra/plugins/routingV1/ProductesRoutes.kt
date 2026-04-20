package cat.montilivi.lallistadelacompra.plugins.routingV1

import cat.montilivi.lallistadelacompra.model.PeticioProducte
import cat.montilivi.lallistadelacompra.model.toCampActualitzable
import cat.montilivi.lallistadelacompra.repositori.RepositoriProductes
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.rutesDelsProductes() {
        route("productes") {

            // GET: Llistar tots els productes
            get {
                val idProducte = call.request.queryParameters["idCategoria"]?.toIntOrNull()
                val llista = if (idProducte != null) {
                    RepositoriProductes.cercaProductesPerCategoria(idProducte)
                } else {
                    RepositoriProductes.obtenTots()
                }
                call.respond(llista)
            }

            // POST: Afegir un producte nou al catàleg
            post {
                val peticio = call.receive<PeticioProducte>()
                if (peticio.nom == null || peticio.idCategoria == null) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Falten dades obligatòries: el 'nom' i la 'idCategoria' són necessaris per crear un producte."
                    )
                }
                else {
                    try {
                        val idProducte = RepositoriProductes.creaProducte(peticio.nom, peticio.idCategoria)
                        call.respond(HttpStatusCode.Created, mapOf("idProducte" to idProducte))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.Conflict, "Aquest producte ja existeix")
                    }
                }
            }

            // PATCH: Modificar un producte
            patch("/{idProducte}") {
                val idProducte = call.parameters["idProducte"]?.toIntOrNull() ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val peticio = call.receive<PeticioProducte>()
                var argNom= peticio.nom.toCampActualitzable()
                var argIdCategoria= peticio.idCategoria.toCampActualitzable()
                val exit = RepositoriProductes.actualitzaProducte(idProducte, argNom, argIdCategoria)
                if (exit) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
            }

            // DELETE: Esborrar un producte
            delete("/{idProducte}") {
                val idProducte = call.parameters["idProducte"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val exit = RepositoriProductes.eliminaProducte(idProducte)
                if (exit) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
            }
        }
}